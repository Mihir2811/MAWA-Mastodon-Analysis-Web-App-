package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.Config;
import models.Account;
import models.Post;
import models.Card;
import models.Attachment;
import models.SearchResult;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Implementation of MastodonService that coordinates caching, remote client queries,
 * and JSON parsing into domain models using built-in Java and SimpleCacheService.
 * The number of posts requested per page (and therefore the page size used by the
 * pagination controls) is read from the {@code mastodon.pageSize} configuration key.
 *
 * @author Kush Chaudhari
 * @author Mihir Vipulbhai Panchal
 * @author Owen Van Esbroeck
 * @author Qiang Xue
 */
@Singleton
public class MastodonServiceImpl implements MastodonService {

    /** Configuration key holding the number of posts requested per page. */
    public static final String PAGE_SIZE_CONFIG_KEY = "mastodon.pageSize";

    /** Page size used when the configuration key is missing or invalid. */
    public static final int DEFAULT_PAGE_SIZE = 5;

    /** Mastodon caps a single timeline request at 40 statuses, so a larger page cannot be served. */
    public static final int MAX_PAGE_SIZE = MastodonClient.MAX_LIMIT;

    /** Cache time-to-live duration in seconds (5 minutes). */
    private static final int CACHE_EXPIRATION_SECONDS = 300; // 5 minutes cache

    /** The MastodonClient used for remote HTTP requests. */
    private final MastodonClient client;
    /** The cache service used to store and retrieve search results. */
    private final SimpleCacheService cache;
    /** The service used to analyze post sentiment. */
    private final SentimentAnalysisService sentimentAnalysisService;
    /** Number of posts requested per page. */
    private final int pageSize;

    /**
     * Constructs a new MastodonServiceImpl.
     *
     * @param client the MastodonClient used for remote HTTP requests
     * @param cache  the SimpleCacheService used for caching search results
     * @param config the application configuration, providing {@code mastodon.pageSize}
     * @param sentimentAnalysisService analyzes each post's readable content
     * @author Qiang Xue
     * @author Mihir Vipulbhai Panchal
     */
    @Inject
    public MastodonServiceImpl(MastodonClient client, SimpleCacheService cache, Config config,
                               SentimentAnalysisService sentimentAnalysisService) {
        this.client = client;
        this.cache = cache;
        this.sentimentAnalysisService = sentimentAnalysisService;
        this.pageSize = readPageSize(config);
    }

    /**
     * Constructs a MastodonServiceImpl with the default sentiment analyzer.
     *
     * @param client the MastodonClient used for remote HTTP requests
     * @param cache the SimpleCacheService used for caching search results
     * @param config the application configuration, providing {@code mastodon.pageSize}
     * @author Qiang Xue
     * @author Mihir Vipulbhai Panchal
     */
    public MastodonServiceImpl(MastodonClient client, SimpleCacheService cache, Config config) {
        this(client, cache, config, new SentimentAnalysisServiceImpl());
    }

    /**
     * Reads the configured page size, falling back to {@link #DEFAULT_PAGE_SIZE} when the key is
     * absent or not a positive integer, and capping it at {@link #MAX_PAGE_SIZE}
     * (the largest number of statuses the Mastodon API returns per request).
     *
     * @param config the application configuration, possibly null
     * @return a usable page size between 1 and 40
     * @author Qiang Xue
     */
    private static int readPageSize(Config config) {
        if (config == null || !config.hasPath(PAGE_SIZE_CONFIG_KEY)) {
            return DEFAULT_PAGE_SIZE;
        }
        try {
            int configured = config.getInt(PAGE_SIZE_CONFIG_KEY);
            if (configured < 1) {
                return DEFAULT_PAGE_SIZE;
            }
            return Math.min(configured, MAX_PAGE_SIZE);
        } catch (RuntimeException notAnInteger) {
            return DEFAULT_PAGE_SIZE;
        }
    }

    /**
     * Returns the number of posts requested per page.
     *
     * @return the configured page size
     * @author Qiang Xue
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * Retrieves one page of posts matching a hashtag, utilizing a cache to avoid duplicate remote requests.
     *
     * @param hashtag the hashtag to search (without '#')
     * @param maxId   optional max_id, used to request the next (older) page
     * @param minId   optional min_id, used to request the previous (newer) page
     * @param page    the 1-based page number being requested
     * @return a CompletionStage holding the SearchResult for that page
     * @author Qiang Xue
     */
    @Override
    public CompletionStage<SearchResult> searchHashtag(String hashtag, Optional<String> maxId,
                                                      Optional<String> minId, int page) {
        if (hashtag == null || hashtag.trim().isEmpty()) {
            return CompletableFuture.completedFuture(
                    new SearchResult("", "Hashtag cannot be empty. Please provide a valid hashtag to search.")
            );
        }

        String cleanTag = hashtag.trim().startsWith("#") ? hashtag.trim().substring(1) : hashtag.trim();
        Optional<String> safeMaxId = maxId != null ? maxId : Optional.empty();
        Optional<String> safeMinId = minId != null ? minId : Optional.empty();
        int safePage = Math.max(page, 1);
        String cacheKey = "tag:" + cleanTag.toLowerCase()
                + ":max_id:" + safeMaxId.orElse("latest")
                + ":min_id:" + safeMinId.orElse("latest")
                + ":page:" + safePage;

        Optional<SearchResult> cachedResult = cache.get(cacheKey);
        if (cachedResult.isPresent()) {
            return CompletableFuture.completedFuture(cachedResult.get());
        }

        return client.getTagTimeline(cleanTag, pageSize, safeMaxId, safeMinId)
                .thenApply(json -> {
                    List<Post> posts = parsePosts(json);
                    SearchResult result = new SearchResult(cleanTag, posts, safePage, safeMaxId, safeMinId, pageSize);
                    cache.set(cacheKey, result, CACHE_EXPIRATION_SECONDS);
                    return result;
                })
                .exceptionally(ex -> new SearchResult(
                        cleanTag,
                        "Unable to retrieve posts from Mastodon: " + (ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage())
                ));
    }

    /**
     * Parses a JSON array node from the Mastodon API into a list of Post domain models.
     *
     * @param json the root JsonNode array
     * @return a list of Post objects
     *
     * @author Qiang Xue
     * @author Owen Van Esbroeck
     */
    public List<Post> parsePosts(JsonNode json) {

        if (json == null || !json.isArray()) {
            return Collections.emptyList();
        }

        List<Post> posts = new ArrayList<>();
        for (JsonNode item : json) {
            //System.out.println(item.toString());
            System.out.println();
            String id = item.path("id").asText("");
            String createdAt = item.path("created_at").asText("");
            String language = item.path("language").isNull() || item.path("language").asText().isEmpty()
                    ? "unknown"
                    : item.path("language").asText();
            String rawContent = item.path("content").asText("");
            String cleanContent = HtmlCleaner.clean(rawContent);
            String url = item.path("url").asText("");
            String visibility = item.path("visibility").asText("public");
            String inReplyToId = item.path("in_reply_to_id").asText("");
            String repliesCount = item.path("replies_count").asText("");
            String reblogsCount = item.path("reblogs_count").asText("");
            String favouritesCount = item.path("favourites_count").asText("");


            List<Attachment> attachments = new ArrayList<>();
            JsonNode attNode = item.path("media_attachments");
            for(JsonNode att : attNode) {
                String attId = att.path("id").asText("");
                String attType = att.path("type").asText("");
                String attUrl = att.path("url").asText("");
                String attPreviewUrl = att.path("preview_url").asText("");
				        String attDesc = att.path("description").asText("");
				        JsonNode meta = att.path("meta").path("small");
				        String attWidth = meta.path("width").asText("0");
				        String attHeight = meta.path("height").asText("0");
                Attachment attachment = new Attachment(attId, attType, attUrl, attPreviewUrl, attDesc, attWidth, attHeight);
                attachments.add(attachment);
            }

			      Card previewCard;
			      JsonNode cardNode = item.path("card");
            if(!cardNode.isNull()) {
                String cardUrl = cardNode.path("url").asText("");
                String cardTitle = cardNode.path("title").asText("");
                String cardDescription = cardNode.path("description").asText("");
                String cardType = cardNode.path("type").asText("");
                String cardWidth = cardNode.path("width").asText("");
                String cardHeight = cardNode.path("height").asText("");
                String cardImage = cardNode.path("image").asText("");
                previewCard = new Card(cardUrl,cardTitle,cardDescription,cardType,cardWidth,cardHeight,cardImage);
            }
            else { previewCard = null; }


            JsonNode accNode = item.path("account");
            String accId = accNode.path("id").asText("");
            String username = accNode.path("username").asText("");
            String displayName = accNode.path("display_name").asText("");
            String accUrl = accNode.path("url").asText("");
            String avatar = accNode.path("avatar").asText("");
            Account author = new Account(accId, username, displayName, accUrl, avatar);

            String sentiment = sentimentAnalysisService.analyze(cleanContent);
            posts.add(
                new Post(id, author, createdAt, language,
                    cleanContent, rawContent,
                    url, visibility,
                    repliesCount, reblogsCount, favouritesCount,
                    sentiment, attachments, previewCard)
                );
        }

        return Collections.unmodifiableList(posts);
    }
}
