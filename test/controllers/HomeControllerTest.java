package controllers;

import models.SearchResult;
import models.AuthorStat;
import models.StatisticalAnalysis;
import models.WordStat;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Result;
import services.MastodonService;
import services.AuthorAnalysisService;
import services.StatisticalAnalysisService;
import services.WordStatsService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static play.mvc.Http.Status.*;
import static play.test.Helpers.contentAsString;

/**
 * Unit tests for HomeController testing all action methods, HTTP status codes,
 * and page-based pagination rendering.
 *
 * @author Kush Chaudhari
 * @author Mihir Vipulbhai Panchal
 * @author Owen Van Esbroeck
 * @author Qiang Xue
 */
public class HomeControllerTest {

    /** Mock MastodonService used to simulate Mastodon operations. */
    private MastodonService mockMastodonService;
    /** Mock AuthorAnalysisService used to simulate author analytics. */
    private AuthorAnalysisService mockAuthorAnalysisService;
    /** Mock WordStatsService used to simulate word statistics computation. */
    private WordStatsService mockWordStatsService;
    /** Mock StatisticalAnalysisService used to simulate post analysis. */
    private StatisticalAnalysisService mockStatisticalAnalysisService;
    /** The HomeController instance under test. */
    private HomeController controller;

    /**
     * Sets up controller with mock services before each test.
     * @author Qiang Xue
     */
    @Before
    public void setUp() {
        mockMastodonService = mock(MastodonService.class);
        mockAuthorAnalysisService = mock(AuthorAnalysisService.class);
        mockWordStatsService = mock(WordStatsService.class);
        mockStatisticalAnalysisService = mock(StatisticalAnalysisService.class);
        when(mockStatisticalAnalysisService.computeAnalysis(any())).thenReturn(
                new StatisticalAnalysis(0, Collections.emptyMap(), Collections.emptyMap(),
                        Collections.emptyMap(), Collections.emptyMap()));
        controller = new HomeController(mockMastodonService, mockAuthorAnalysisService,
                mockWordStatsService, mockStatisticalAnalysisService);
    }

    /**
     * Tests that index() renders the welcome page with 200 OK.
     * @author Qiang Xue
     */
    @Test
    public void testIndexAction() throws Exception {
        Result result = controller.index().toCompletableFuture().get();
        assertEquals(OK, result.status());
        assertTrue(contentAsString(result).contains("Mastodon Hashtag Search"));
    }

    /**
     * Tests search action with empty hashtag input returns 400 Bad Request.
     * @author Qiang Xue
     */
    @Test
    public void testSearchActionWithEmptyInput() throws Exception {
        Result result = controller.search("", Optional.empty(), Optional.empty(), 1).toCompletableFuture().get();
        assertEquals(BAD_REQUEST, result.status());
        assertTrue(contentAsString(result).contains("Please enter a valid hashtag"));

        Result resultNull = controller.search(null, Optional.empty(), Optional.empty(), 1).toCompletableFuture().get();
        assertEquals(BAD_REQUEST, resultNull.status());
    }

    /**
     * Tests search action with valid hashtag returns 200 OK and renders post data.
     * @author Qiang Xue
     */
    @Test
    public void testSearchActionSuccess() throws Exception {
        SearchResult searchResult = new SearchResult("java", Collections.emptyList(), 1,
                Optional.empty(), Optional.empty(), 40);
        when(mockMastodonService.searchHashtag(eq("java"), any(), any(), eq(1)))
                .thenReturn(CompletableFuture.completedFuture(searchResult));

        Result result = controller.search("java", Optional.empty(), Optional.empty(), 1).toCompletableFuture().get();
        assertEquals(OK, result.status());
        assertTrue(contentAsString(result).contains("#java"));
    }

    /**
     * Tests that each search result displays the sentiment assigned to its post.
     * @author Mihir Vipulbhai Panchal
     */
    @Test
    public void testSearchActionDisplaysNegativeSentiment() throws Exception {
        models.Post negativePost = new models.Post("1",
                new models.Account("1", "alice", "Alice", "url", "avatar"),
                "time", "en", "This is crap, and it isn't great.",
                "<p>This is crap, and it isn't great.</p>", "url", "public",
                null, null, null, ":-(", null, null);
        SearchResult searchResult = new SearchResult("java", Collections.singletonList(negativePost), 1,
                Optional.empty(), Optional.empty(), 40);
        when(mockMastodonService.searchHashtag(eq("java"), any(), any(), eq(1)))
                .thenReturn(CompletableFuture.completedFuture(searchResult));

        String html = contentAsString(controller.search("java", Optional.empty(), Optional.empty(), 1)
                .toCompletableFuture().get());

        assertTrue(html.contains("title=\"sentiment\">:-(</span>"));
    }

    /**
     * Tests that null pagination parameters are treated as "no anchor", i.e. page 1.
     * @author Qiang Xue
     */
    @Test
    public void testSearchActionWithNullPaginationParameters() throws Exception {
        SearchResult searchResult = new SearchResult("java", Collections.emptyList(), 1,
                Optional.empty(), Optional.empty(), 40);
        when(mockMastodonService.searchHashtag(eq("java"), eq(Optional.empty()), eq(Optional.empty()), eq(1)))
                .thenReturn(CompletableFuture.completedFuture(searchResult));

        Result result = controller.search("java", null, null, 1).toCompletableFuture().get();

        assertEquals(OK, result.status());
        verify(mockMastodonService).searchHashtag("java", Optional.empty(), Optional.empty(), 1);
    }

    /**
     * Tests that a page number lower than 1 is normalised to page 1.
     * @author Qiang Xue
     */
    @Test
    public void testSearchActionWithZeroPageDefaultsToOne() throws Exception {
        SearchResult searchResult = new SearchResult("java", Collections.emptyList(), 1,
                Optional.empty(), Optional.empty(), 40);
        when(mockMastodonService.searchHashtag(eq("java"), any(), any(), eq(1)))
                .thenReturn(CompletableFuture.completedFuture(searchResult));

        Result result = controller.search("java", Optional.empty(), Optional.empty(), 0).toCompletableFuture().get();

        assertEquals(OK, result.status());
        verify(mockMastodonService).searchHashtag("java", Optional.empty(), Optional.empty(), 1);
    }

    /**
     * Tests that a hashtag consisting only of a '#' character is rejected with 400 Bad Request.
     * @author Qiang Xue
     */
    @Test
    public void testSearchActionWithHashOnlyIsRejected() throws Exception {
        Result result = controller.search("#", Optional.empty(), Optional.empty(), 1).toCompletableFuture().get();

        assertEquals(BAD_REQUEST, result.status());
        verifyNoInteractions(mockMastodonService);
    }

    /**
     * Tests that a hashtag containing a '#' in the middle ("c#") is rejected instead of being sent
     * to Mastodon, where the '#' would start a URL fragment and silently drop the limit parameter.
     * @author Qiang Xue
     */
    @Test
    public void testSearchActionWithInnerHashIsRejected() throws Exception {
        Result result = controller.search("c#", Optional.empty(), Optional.empty(), 1).toCompletableFuture().get();

        assertEquals(BAD_REQUEST, result.status());
        assertTrue(contentAsString(result).contains("Please enter a valid hashtag"));
        verifyNoInteractions(mockMastodonService);
    }

    /**
     * Tests that other characters which are not part of a Mastodon hashtag are rejected.
     * @author Qiang Xue
     */
    @Test
    public void testSearchActionWithInvalidCharactersIsRejected() throws Exception {
        Result spaced = controller.search("hello world", Optional.empty(), Optional.empty(), 1)
                .toCompletableFuture().get();
        assertEquals(BAD_REQUEST, spaced.status());

        Result slashed = controller.search("java/lang", Optional.empty(), Optional.empty(), 1)
                .toCompletableFuture().get();
        assertEquals(BAD_REQUEST, slashed.status());

        verifyNoInteractions(mockMastodonService);
    }

    /**
     * Tests that accented hashtags (letters outside the ASCII range) are accepted and forwarded as-is.
     * @author Qiang Xue
     */
    @Test
    public void testSearchActionAcceptsAccentedHashtag() throws Exception {
        SearchResult searchResult = new SearchResult("café", Collections.emptyList(), 1,
                Optional.empty(), Optional.empty(), 40);
        when(mockMastodonService.searchHashtag(eq("café"), any(), any(), eq(1)))
                .thenReturn(CompletableFuture.completedFuture(searchResult));

        Result result = controller.search("café", Optional.empty(), Optional.empty(), 1).toCompletableFuture().get();

        assertEquals(OK, result.status());
        verify(mockMastodonService).searchHashtag("café", Optional.empty(), Optional.empty(), 1);
    }

    /**
     * Tests that a leading '#' is still accepted and stripped before the query.
     * @author Qiang Xue
     */
    @Test
    public void testSearchActionStripsLeadingHash() throws Exception {
        SearchResult searchResult = new SearchResult("java", Collections.emptyList(), 1,
                Optional.empty(), Optional.empty(), 40);
        when(mockMastodonService.searchHashtag(eq("java"), any(), any(), eq(1)))
                .thenReturn(CompletableFuture.completedFuture(searchResult));

        Result result = controller.search("#java", Optional.empty(), Optional.empty(), 1).toCompletableFuture().get();

        assertEquals(OK, result.status());
        verify(mockMastodonService).searchHashtag("java", Optional.empty(), Optional.empty(), 1);
    }

    /**
     * Tests that the word statistics action also rejects an invalid hashtag.
     * @author Qiang Xue
     */
    @Test
    public void testWordStatsActionWithInvalidHashtagIsRejected() throws Exception {
        Result result = controller.wordStats("c#", Optional.empty(), Optional.empty(), 1)
                .toCompletableFuture().get();

        assertEquals(BAD_REQUEST, result.status());
        assertTrue(contentAsString(result).contains("Please provide a valid hashtag"));
        verifyNoInteractions(mockMastodonService);
    }

    /**
     * Tests search action when service returns an error result renders 500 Internal Server Error.
     * @author Qiang Xue
     */
    @Test
    public void testSearchActionError() throws Exception {
        SearchResult errorResult = new SearchResult("java", "Connection timeout");
        when(mockMastodonService.searchHashtag(eq("java"), any(), any(), anyInt()))
                .thenReturn(CompletableFuture.completedFuture(errorResult));

        Result result = controller.search("java", Optional.empty(), Optional.empty(), 1).toCompletableFuture().get();
        assertEquals(INTERNAL_SERVER_ERROR, result.status());
        assertTrue(contentAsString(result).contains("Connection timeout"));
    }

    /**
     * Tests that page 1 renders a disabled Previous link and an enabled Next link
     * when a full page of posts is returned.
     * @author Qiang Xue
     */
    @Test
    public void testSearchActionRendersNextLinkOnFullPage() throws Exception {
        SearchResult searchResult = new SearchResult("java",
                posts("40", "30", "20", "10"), 1, Optional.empty(), Optional.empty(), 4);
        when(mockMastodonService.searchHashtag(eq("java"), any(), any(), eq(1)))
                .thenReturn(CompletableFuture.completedFuture(searchResult));

        String html = contentAsString(controller.search("java", Optional.empty(), Optional.empty(), 1)
                .toCompletableFuture().get());

        assertTrue(html.contains("Page 1"));
        assertFalse(html.contains("id=\"previous-page-link\""));
        assertTrue(html.contains("id=\"next-page-link\""));
        assertTrue(html.contains("max_id=10"));
    }

    /**
     * Tests that page 2 renders both Previous (using min_id of the first post) and Next links.
     * @author Qiang Xue
     */
    @Test
    public void testSearchActionRendersPreviousAndNextLinks() throws Exception {
        SearchResult searchResult = new SearchResult("java",
                posts("80", "70", "60", "50"), 2, Optional.of("90"), Optional.empty(), 4);
        when(mockMastodonService.searchHashtag(eq("java"), any(), any(), eq(2)))
                .thenReturn(CompletableFuture.completedFuture(searchResult));

        String html = contentAsString(controller.search("java", Optional.of("90"), Optional.empty(), 2)
                .toCompletableFuture().get());

        assertTrue(html.contains("Page 2"));
        assertTrue(html.contains("id=\"previous-page-link\""));
        assertTrue(html.contains("min_id=80"));
        // page=1 equals the route default, so it is omitted from the previous link
        assertTrue(html.matches("(?s).*href=\"/search\\?hashtag=java&amp;min_id=80\".*"));
        assertTrue(html.contains("id=\"next-page-link\""));
        assertTrue(html.contains("max_id=50"));
        assertTrue(html.contains("page=3"));
    }

    /**
     * Tests that the word statistics link on a search page carries the current page's
     * max_id / min_id / page values, so the same page is re-used (from cache).
     * @author Qiang Xue
     */
    @Test
    public void testSearchActionWordStatsLinkCarriesPageParameters() throws Exception {
        SearchResult searchResult = new SearchResult("java",
                posts("80", "70"), 2, Optional.of("90"), Optional.of("95"), 2);
        when(mockMastodonService.searchHashtag(eq("java"), any(), any(), eq(2)))
                .thenReturn(CompletableFuture.completedFuture(searchResult));

        String html = contentAsString(controller.search("java", Optional.of("90"), Optional.of("95"), 2)
                .toCompletableFuture().get());

        assertTrue(html.contains("/wordstats?hashtag=java&amp;max_id=90&amp;min_id=95&amp;page=2")
                || html.contains("/wordstats?hashtag=java&max_id=90&min_id=95&page=2"));
    }

    /**
     * Tests that the author-analysis link on a search page carries the current page's
     * max_id / min_id / page values, so the same page is re-used (from cache).
     *
     * @author Kush Chaudhari
     */
    @Test
    public void testSearchActionAuthorAnalysisLinkCarriesPageParameters() throws Exception {
        SearchResult searchResult = new SearchResult("java",
                posts("80", "70"), 2, Optional.of("90"), Optional.of("95"), 2);
        when(mockMastodonService.searchHashtag(eq("java"), any(), any(), eq(2)))
                .thenReturn(CompletableFuture.completedFuture(searchResult));

        String html = contentAsString(controller.search("java", Optional.of("90"), Optional.of("95"), 2)
                .toCompletableFuture().get());

        assertTrue(html.contains("id=\"author-analysis-btn\""));
        assertTrue(html.contains("Analyze Authors on This Page"));
        assertTrue(html.contains("/authoranalysis?hashtag=java&amp;max_id=90&amp;min_id=95&amp;page=2")
                || html.contains("/authoranalysis?hashtag=java&max_id=90&min_id=95&page=2"));
    }

    /**
     * Tests that the author-analysis action renders the top authors for the requested page.
     *
     * @author Kush Chaudhari
     */
    @Test
    public void testAuthorAnalysisActionDisplaysTopAuthors() throws Exception {
        SearchResult searchResult = new SearchResult("java",
                posts("80", "70"), 2, Optional.of("90"), Optional.empty(), 2);
        when(mockMastodonService.searchHashtag("java", Optional.of("90"), Optional.empty(), 2))
                .thenReturn(CompletableFuture.completedFuture(searchResult));
        when(mockAuthorAnalysisService.computeAuthorStats(any()))
                .thenReturn(List.of(
                        new AuthorStat(new models.Account("1", "alice", "Alice", "https://example.com/alice", "avatar"),
                                2, searchResult.getPosts().get(0), Collections.singletonMap("en", 2L))));

        Result result = controller.authorAnalysis("java", Optional.of("90"), Optional.empty(), 2)
                .toCompletableFuture().get();

        assertEquals(OK, result.status());
        String html = contentAsString(result);
        assertTrue(html.contains("Authors on Page 2"));
        assertTrue(html.contains("Alice"));
        assertTrue(html.contains("2 post(s)"));
        assertTrue(html.contains("Account ID: 1"));
        assertTrue(html.contains("View latest post"));
        assertTrue(html.contains("EN"));
        verify(mockMastodonService).searchHashtag("java", Optional.of("90"), Optional.empty(), 2);
        verify(mockAuthorAnalysisService).computeAuthorStats(searchResult.getPosts());
    }

    /**
     * Tests that author analysis preserves both pagination anchors in its request and back link.
     *
     * @author Kush Chaudhari
     */
    @Test
    public void testAuthorAnalysisPreservesBothPaginationAnchors() throws Exception {
        SearchResult searchResult = new SearchResult("java",
                posts("80", "70"), 2, Optional.of("90"), Optional.of("95"), 2);
        when(mockMastodonService.searchHashtag("java", Optional.of("90"), Optional.of("95"), 2))
                .thenReturn(CompletableFuture.completedFuture(searchResult));
        when(mockAuthorAnalysisService.computeAuthorStats(searchResult.getPosts()))
                .thenReturn(Collections.emptyList());

        Result result = controller.authorAnalysis("java", Optional.of("90"), Optional.of("95"), 2)
                .toCompletableFuture().get();

        assertEquals(OK, result.status());
        String html = contentAsString(result);
        assertTrue(html.contains("href=\"/search?hashtag=java&amp;max_id=90&amp;min_id=95&amp;page=2\"")
                || html.contains("href=\"/search?hashtag=java&max_id=90&min_id=95&page=2\""));
        verify(mockMastodonService).searchHashtag("java", Optional.of("90"), Optional.of("95"), 2);
    }

    /**
     * Tests that zero and negative page numbers are normalized to page 1.
     *
     * @author Kush Chaudhari
     */
    @Test
    public void testAuthorAnalysisNormalizesNonPositivePageNumbers() throws Exception {
        SearchResult searchResult = new SearchResult("java", Collections.emptyList(), 1,
                Optional.empty(), Optional.empty(), 40);
        when(mockMastodonService.searchHashtag("java", Optional.empty(), Optional.empty(), 1))
                .thenReturn(CompletableFuture.completedFuture(searchResult));
        when(mockAuthorAnalysisService.computeAuthorStats(searchResult.getPosts()))
                .thenReturn(Collections.emptyList());

        Result zeroPageResult = controller.authorAnalysis("java", Optional.empty(), Optional.empty(), 0)
                .toCompletableFuture().get();
        Result negativePageResult = controller.authorAnalysis("java", Optional.empty(), Optional.empty(), -3)
                .toCompletableFuture().get();

        assertEquals(OK, zeroPageResult.status());
        assertEquals(OK, negativePageResult.status());
        assertTrue(contentAsString(zeroPageResult).contains("Page 1"));
        assertTrue(contentAsString(negativePageResult).contains("Page 1"));
        verify(mockMastodonService, times(2))
                .searchHashtag("java", Optional.empty(), Optional.empty(), 1);
    }

    /**
     * Tests that author-provided names and post text are rendered as escaped HTML text.
     *
     * @author Kush Chaudhari
     */
    @Test
    public void testAuthorAnalysisEscapesAuthorAndPostHtml() throws Exception {
        models.Account author = new models.Account("1", "alice", "<script>alert(1)</script>",
                "https://example.com/alice", "");
        models.Post post = new models.Post("1", author, "time", "en",
                "<img src=x onerror=alert(1)>", "", "https://example.com/post", "public");
        SearchResult searchResult = new SearchResult("java", Collections.singletonList(post), 1,
                Optional.empty(), Optional.empty(), 40);
        when(mockMastodonService.searchHashtag("java", Optional.empty(), Optional.empty(), 1))
                .thenReturn(CompletableFuture.completedFuture(searchResult));
        when(mockAuthorAnalysisService.computeAuthorStats(searchResult.getPosts()))
                .thenReturn(Collections.singletonList(new AuthorStat(author, 1, post,
                        Collections.singletonMap("en", 1L))));

        Result result = controller.authorAnalysis("java", Optional.empty(), Optional.empty(), 1)
                .toCompletableFuture().get();

        assertEquals(OK, result.status());
        String html = contentAsString(result);
        assertTrue(html.contains("&lt;script&gt;alert(1)&lt;/script&gt;"));
        assertTrue(html.contains("&lt;img src=x onerror=alert(1)&gt;"));
        assertFalse(html.contains("<script>alert(1)</script>"));
        assertFalse(html.contains("<img src=x onerror=alert(1)>"));
    }

    /**
     * Tests that an empty search page renders the author-analysis empty state without a ranking table.
     *
     * @author Kush Chaudhari
     */
    @Test
    public void testAuthorAnalysisDisplaysEmptyResultsState() throws Exception {
        SearchResult searchResult = new SearchResult("java", Collections.emptyList(), 1,
                Optional.empty(), Optional.empty(), 40);
        when(mockMastodonService.searchHashtag("java", Optional.empty(), Optional.empty(), 1))
                .thenReturn(CompletableFuture.completedFuture(searchResult));
        when(mockAuthorAnalysisService.computeAuthorStats(searchResult.getPosts()))
                .thenReturn(Collections.emptyList());

        Result result = controller.authorAnalysis("java", Optional.empty(), Optional.empty(), 1)
                .toCompletableFuture().get();

        assertEquals(OK, result.status());
        String html = contentAsString(result);
        assertTrue(html.contains("No author data available"));
        assertTrue(html.contains("No author activity to rank"));
        assertFalse(html.contains("Authors on Page 1"));
        verify(mockAuthorAnalysisService).computeAuthorStats(searchResult.getPosts());
    }

    /**
     * Tests that an upstream search error is displayed on the author-analysis page with a 500 status.
     *
     * @author Kush Chaudhari
     */
    @Test
    public void testAuthorAnalysisErrorIsDisplayed() throws Exception {
        when(mockMastodonService.searchHashtag("java", Optional.empty(), Optional.empty(), 1))
                .thenReturn(CompletableFuture.completedFuture(
                        new SearchResult("java", "Mastodon rate limit exceeded")));

        Result result = controller.authorAnalysis("java", Optional.empty(), Optional.empty(), 1)
                .toCompletableFuture().get();

        assertEquals(INTERNAL_SERVER_ERROR, result.status());
        assertTrue(contentAsString(result).contains("Mastodon rate limit exceeded"));
        verifyNoInteractions(mockAuthorAnalysisService);
    }

    /**
     * Tests that the author-analysis action rejects an invalid hashtag with 400 Bad Request.
     *
     * @author Kush Chaudhari
     */
    @Test
    public void testAuthorAnalysisRejectsInvalidHashtag() throws Exception {
        Result result = controller.authorAnalysis("c#", Optional.empty(), Optional.empty(), 1)
                .toCompletableFuture().get();

        assertEquals(BAD_REQUEST, result.status());
        assertTrue(contentAsString(result).contains("Please provide a valid hashtag"));
        verifyNoInteractions(mockMastodonService, mockAuthorAnalysisService);
    }

    /**
     * Builds a list of posts with the given ids.
     *
     * @param ids the post ids, newest first
     * @return list of Post instances
     * @author Qiang Xue
     */
    private List<models.Post> posts(String... ids) {
        List<models.Post> result = new java.util.ArrayList<>();
        for (String id : ids) {
            result.add(new models.Post(id,
                    new models.Account("1", "alice", "Alice", "url", "avatar"),
                    "time", "en", "content", "<p>content</p>", "url", "public"));
        }
        return result;
    }

    /**
     * Tests wordStats action with null or empty hashtag returns 400 Bad Request.
     * @author Qiang Xue
     */
    @Test
    public void testWordStatsActionWithEmptyInput() throws Exception {
        Result r1 = controller.wordStats("", Optional.empty(), Optional.empty(), 1).toCompletableFuture().get();
        assertEquals(BAD_REQUEST, r1.status());
        assertTrue(contentAsString(r1).contains("Please provide a valid hashtag"));

        Result r2 = controller.wordStats(null, Optional.empty(), Optional.empty(), 1).toCompletableFuture().get();
        assertEquals(BAD_REQUEST, r2.status());
    }

    /**
     * Tests wordStats action with valid hashtag returns 200 OK and rendered statistics.
     * @author Qiang Xue
     */
    @Test
    public void testWordStatsActionSuccess() throws Exception {
        SearchResult searchResult = new SearchResult("java", Collections.emptyList(), 1,
                Optional.empty(), Optional.empty(), 40);
        when(mockMastodonService.searchHashtag(eq("java"), any(), any(), eq(1)))
                .thenReturn(CompletableFuture.completedFuture(searchResult));
        when(mockWordStatsService.computeWordStats(any()))
                .thenReturn(List.of(new WordStat("programming", 15)));

        Result result = controller.wordStats("#java", Optional.empty(), Optional.empty(), 1).toCompletableFuture().get();
        assertEquals(OK, result.status());
        assertTrue(contentAsString(result).contains("Word Statistics for"));
        assertTrue(contentAsString(result).contains("programming"));
        assertTrue(contentAsString(result).contains("15"));
    }

    /**
     * Tests that the word statistics page requests the same page of posts (same max_id / min_id /
     * page) and shows the page number.
     * @author Qiang Xue
     */
    @Test
    public void testWordStatsActionUsesSamePage() throws Exception {
        SearchResult searchResult = new SearchResult("java", Collections.emptyList(), 3,
                Optional.of("55"), Optional.empty(), 40);
        when(mockMastodonService.searchHashtag(eq("java"), eq(Optional.of("55")), eq(Optional.empty()), eq(3)))
                .thenReturn(CompletableFuture.completedFuture(searchResult));
        when(mockWordStatsService.computeWordStats(any())).thenReturn(List.of(new WordStat("java", 2)));

        Result result = controller.wordStats("java", Optional.of("55"), Optional.empty(), 3)
                .toCompletableFuture().get();

        assertEquals(OK, result.status());
        String html = contentAsString(result);
        assertTrue(html.contains("Word Statistics for"));
        assertTrue(html.contains("Page 3"));
        verify(mockMastodonService).searchHashtag("java", Optional.of("55"), Optional.empty(), 3);
    }

    /**
     * Tests wordStats action when upstream search returns an error returns 500 Internal Server Error.
     * @author Qiang Xue
     */
    @Test
    public void testWordStatsActionError() throws Exception {
        SearchResult errorResult = new SearchResult("java", "Network failure");
        when(mockMastodonService.searchHashtag(eq("java"), any(), any(), eq(1)))
                .thenReturn(CompletableFuture.completedFuture(errorResult));

        Result result = controller.wordStats("java", Optional.empty(), Optional.empty(), 1)
                .toCompletableFuture().get();
        assertEquals(INTERNAL_SERVER_ERROR, result.status());
        assertTrue(contentAsString(result).contains("Network failure"));
    }
}
