package controllers;

import models.AuthorStat;
import models.SearchResult;
import models.WordStat;
import models.StatisticalAnalysis;
import play.mvc.Controller;
import play.mvc.Result;
import services.AuthorAnalysisService;
import services.MastodonService;
import services.WordStatsService;
import services.StatisticalAnalysisService;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.regex.Pattern;

/**
 * Single controller of the application. It exposes the hashtag search (with page-based
 * pagination), word statistics, and author analysis pages, and delegates logic to services.
 * Every action is non-blocking and returns a {@link java.util.concurrent.CompletionStage}.
 *
 * @author Kush Chaudhari
 * @author Mihir Vipulbhai Panchal
 * @author Owen Van Esbroeck
 * @author Qiang Xue
 */
public class HomeController extends Controller {

    /** Mastodon hashtags consist of letters, digits and underscores only. */
    private static final Pattern VALID_HASHTAG = Pattern.compile("[\\p{L}\\p{Nd}_]+");

    /** The MastodonService used for retrieving hashtag search results. */
    private final MastodonService mastodonService;
    /** The AuthorAnalysisService used for calculating author contributions. */
    private final AuthorAnalysisService authorAnalysisService;
    /** The WordStatsService used for calculating word frequencies. */
    private final WordStatsService wordStatsService;
    /** The StatisticalAnalysisService used for computing post statistics. */
    private final StatisticalAnalysisService statisticalAnalysisService;

    /**
     * Constructs a new HomeController with its injected services.
     *
     * @param mastodonService      the MastodonService instance
     * @param authorAnalysisService the AuthorAnalysisService instance
     * @param wordStatsService      the WordStatsService instance
     * @param statisticalAnalysisService the StatisticalAnalysisService instance
     * @author Kush Chaudhari
     * @author Mihir Vipulbhai Panchal
     * @author Owen Van Esbroeck
     * @author Qiang Xue
     */
    @Inject
    public HomeController(MastodonService mastodonService, AuthorAnalysisService authorAnalysisService,
                          WordStatsService wordStatsService, StatisticalAnalysisService statisticalAnalysisService) {
        this.mastodonService = mastodonService;
        this.authorAnalysisService = authorAnalysisService;
        this.wordStatsService = wordStatsService;
        this.statisticalAnalysisService = statisticalAnalysisService;
    }

    /**
     * Renders the search home page.
     *
     * @return CompletionStage of 200 OK with the home page view
     * @author Qiang Xue
     */
    public CompletionStage<Result> index() {
        return CompletableFuture.completedFuture(ok(views.html.index.render()));
    }

    /**
     * Asynchronously handles a hashtag search for one result page.
     * The page is selected with Mastodon's {@code max_id} (next/older page) and
     * {@code min_id} (previous/newer page) parameters.
     *
     * @param hashtag the hashtag to search, with or without the leading '#'
     * @param maxId   optional anchor for the next (older) page
     * @param minId   optional anchor for the previous (newer) page
     * @param page    the 1-based page number being displayed
     * @return CompletionStage of 200 OK, 400 Bad Request, or 500 Internal Server Error
     * @author Qiang Xue
     */
    public CompletionStage<Result> search(String hashtag, Optional<String> maxId, Optional<String> minId, int page) {
        String tag = cleanHashtag(hashtag);
        if (tag.isEmpty()) {
            SearchResult errorResult = new SearchResult("", "Please enter a valid hashtag to search (letters, digits and underscores only).");
            return CompletableFuture.completedFuture(badRequest(views.html.search.render(errorResult,null)));
        }

        return mastodonService.searchHashtag(tag, normalize(maxId), normalize(minId), Math.max(page, 1))
                .thenApply(searchResult -> {
                    if (searchResult.hasError()) {
                        return internalServerError(views.html.search.render(searchResult, null));
                    }
                    StatisticalAnalysis analysis = statisticalAnalysisService.computeAnalysis(searchResult.getPosts());
                    return ok(views.html.search.render(searchResult, analysis));
                });
    }

    /**
     * Asynchronously calculates and renders word-level statistics for the posts of one result page.
     * The same {@code max_id} / {@code min_id} / {@code page} values used by the search page are
     * passed through, so the service serves that page from its cache instead of calling Mastodon again.
     *
     * @param hashtag the hashtag query string, with or without the leading '#'
     * @param maxId   optional anchor of the page whose statistics are requested
     * @param minId   optional anchor of the page whose statistics are requested
     * @param page    the 1-based page number whose statistics are requested
     * @return CompletionStage of 200 OK, 400 Bad Request, or 500 Internal Server Error
     * @author Qiang Xue
     */
    public CompletionStage<Result> wordStats(String hashtag, Optional<String> maxId, Optional<String> minId, int page) {
        Optional<String> safeMaxId = normalize(maxId);
        Optional<String> safeMinId = normalize(minId);
        int safePage = Math.max(page, 1);
        String tag = cleanHashtag(hashtag);

        if (tag.isEmpty()) {
            return CompletableFuture.completedFuture(
                    badRequest(views.html.wordstats.render("", Collections.emptyList(),
                            Optional.of("Please provide a valid hashtag (letters, digits and underscores only)."), safePage, safeMaxId, safeMinId))
            );
        }

        return mastodonService.searchHashtag(tag, safeMaxId, safeMinId, safePage)
                .thenApply(searchResult -> {
                    if (searchResult.hasError()) {
                        return internalServerError(views.html.wordstats.render(tag, Collections.emptyList(),
                                searchResult.getErrorMessage(), safePage, safeMaxId, safeMinId));
                    }
                    List<WordStat> stats = wordStatsService.computeWordStats(searchResult.getPosts());
                    return ok(views.html.wordstats.render(tag, stats, Optional.empty(),
                            safePage, safeMaxId, safeMinId));
                });
    }

    /**
     * Asynchronously calculates author contributions for the requested search-result page.
     *
     * @param hashtag the hashtag query string, with or without the leading '#'
     * @param maxId   optional anchor of the page whose author statistics are requested
     * @param minId   optional anchor of the page whose author statistics are requested
     * @param page    the 1-based page number whose author statistics are requested
     * @return CompletionStage of 200 OK, 400 Bad Request, or 500 Internal Server Error
     * @author Kush Chaudhari
     */
    public CompletionStage<Result> authorAnalysis(String hashtag, Optional<String> maxId,
                                                  Optional<String> minId, int page) {
        Optional<String> safeMaxId = normalize(maxId);
        Optional<String> safeMinId = normalize(minId);
        int safePage = Math.max(page, 1);
        String tag = cleanHashtag(hashtag);

        if (tag.isEmpty()) {
            return CompletableFuture.completedFuture(
                    badRequest(views.html.authoranalysis.render("", Collections.emptyList(),
                            Optional.of("Please provide a valid hashtag (letters, digits and underscores only)."),
                            0, safePage, safeMaxId, safeMinId))
            );
        }

        return mastodonService.searchHashtag(tag, safeMaxId, safeMinId, safePage)
                .thenApply(searchResult -> {
                    if (searchResult.hasError()) {
                        return internalServerError(views.html.authoranalysis.render(tag, Collections.emptyList(),
                                searchResult.getErrorMessage(), searchResult.getPosts().size(),
                                safePage, safeMaxId, safeMinId));
                    }
                    List<AuthorStat> stats = authorAnalysisService.computeAuthorStats(searchResult.getPosts());
                    return ok(views.html.authoranalysis.render(tag, stats, Optional.empty(),
                            searchResult.getPosts().size(), safePage, safeMaxId, safeMinId));
                });
    }

    /**
     * Trims a hashtag, removes an optional leading '#' and verifies that the remainder is a valid
     * Mastodon hashtag (letters, digits and underscores). Invalid input — e.g. {@code "c#"},
     * which would otherwise be sent as {@code /timelines/tag/c#?limit=...} where everything after
     * the '#' becomes a URL fragment — is rejected so the user gets a 400 instead of a surprising result.
     *
     * @param hashtag the raw hashtag entered by the user, possibly null
     * @return the cleaned hashtag, or an empty string when no valid hashtag was provided
     * @author Qiang Xue
     */
    private static String cleanHashtag(String hashtag) {
        if (hashtag == null) {
            return "";
        }
        String trimmed = hashtag.trim();
        String withoutHash = trimmed.startsWith("#") ? trimmed.substring(1).trim() : trimmed;
        return VALID_HASHTAG.matcher(withoutHash).matches() ? withoutHash : "";
    }

    /**
     * Replaces a null optional query parameter with an empty one.
     *
     * @param value the optional request parameter, possibly null
     * @return the given optional, or an empty optional when null
     * @author Qiang Xue
     */
    private static Optional<String> normalize(Optional<String> value) {
        return value != null ? value : Optional.empty();
    }
}
