package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.Config;
import play.libs.Json;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

/**
 * Implementation of MastodonClient using standard Java SE 11+ java.net.http.HttpClient.
 * Pure Java non-blocking REST client without any third-party HTTP wrappers.
 *
 * @author Qiang Xue
 */
@Singleton
public class MastodonClientImpl implements MastodonClient {

    /** Standard Java HttpClient used to issue asynchronous HTTP requests. */
    private final HttpClient httpClient;
    /** The target Mastodon instance hostname (e.g. mastodon.social). */
    private final String instance;
    /** The Bearer access token for authenticated API requests, if configured. */
    private final String token;

    /**
     * Constructs MastodonClientImpl using application configuration and default HttpClient.
     *
     * @param config the application configuration
     * @author Qiang Xue
     */
    @Inject
    public MastodonClientImpl(Config config) {
        this(config, HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build());
    }

    /**
     * Constructs MastodonClientImpl with an injected or mock HttpClient.
     *
     * @param config     the application configuration
     * @param httpClient standard Java HttpClient instance
     * @author Qiang Xue
     */
    public MastodonClientImpl(Config config, HttpClient httpClient) {
        this.httpClient = httpClient;
        this.instance = config.hasPath("mastodon.instance") && !config.getString("mastodon.instance").trim().isEmpty()
                ? config.getString("mastodon.instance").trim()
                : "mastodon.social";
        this.token = config.hasPath("mastodon.token")
                ? config.getString("mastodon.token").trim()
                : "";
    }

    /**
     * Retrieves public posts for a given hashtag from the Mastodon REST API asynchronously.
     *
     * @param hashtag the hashtag to query (without the '#' symbol)
     * @param limit   maximum number of posts to retrieve, capped at {@code MAX_LIMIT} (40)
     * @param maxId   optional max_id token, requesting results older than this status id
     * @param minId   optional min_id token, requesting results newer than this status id
     * @return a CompletionStage containing the parsed JsonNode array of statuses
     * @author Qiang Xue
     */
    @Override
    public CompletionStage<JsonNode> getTagTimeline(String hashtag, int limit, Optional<String> maxId, Optional<String> minId) {
        String cleanTag = hashtag.startsWith("#") ? hashtag.substring(1) : hashtag;
        StringBuilder uriBuilder = new StringBuilder();
        uriBuilder.append("https://")
                  .append(instance)
                  .append("/api/v1/timelines/tag/")
                  .append(encode(cleanTag))
                  .append("?limit=")
                  .append(Math.min(limit, MAX_LIMIT));

        // max_id -> the next (older) page; min_id -> the previous (newer) page
        if (maxId != null && maxId.isPresent() && !maxId.get().trim().isEmpty()) {
            uriBuilder.append("&max_id=").append(encode(maxId.get().trim()));
        }
        if (minId != null && minId.isPresent() && !minId.get().trim().isEmpty()) {
            uriBuilder.append("&min_id=").append(encode(minId.get().trim()));
        }

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(uriBuilder.toString()))
                .timeout(Duration.ofSeconds(10))
                .header("Accept", "application/json")
                .GET();

        if (!token.isEmpty()) {
            requestBuilder.header("Authorization", "Bearer " + token);
        }

        return httpClient.sendAsync(requestBuilder.build(), HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    int status = response.statusCode();
                    if (status != 200) {
                        throw new RuntimeException("Mastodon API returned status " + status + ": " + response.body());
                    }
                    return Json.parse(response.body());
                });
    }

    /**
     * Percent-encodes a value for use inside a request URI, so characters that are significant in
     * URLs (notably '#', which would start a fragment and hide the query string, and non-ASCII
     * letters such as 'é') cannot break the request. Spaces are encoded as %20 rather than '+'.
     *
     * @param value the raw value
     * @return the encoded value
     * @author Qiang Xue
     */
    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    /**
     * Returns the configured instance hostname.
     *
     * @return instance hostname
     * @author Qiang Xue
     */
    public String getInstance() {
        return instance;
    }
}
