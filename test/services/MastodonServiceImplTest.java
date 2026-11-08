package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.ConfigFactory;
import models.Attachment;
import models.Post;
import models.SearchResult;
import org.junit.Before;
import org.junit.Test;

import org.mockito.ArgumentCaptor;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MastodonServiceImpl, testing caching mechanisms, JSON-to-model transformations,
 * error handling, and page-based pagination handling with Mockito and SimpleCacheService.
 *
 * @author Kush Chaudhari
 * @author Mihir Vipulbhai Panchal
 * @author Owen Van Esbroeck
 * @author Qiang Xue
 */
public class MastodonServiceImplTest {

    /** Page size used throughout the tests, independent of the application configuration. */
    private static final int TEST_PAGE_SIZE = 40;

    /** Mock MastodonClient used for intercepting HTTP client calls. */
    private MastodonClient mockClient;
    /** Mock SimpleCacheService used for cache verification. */
    private SimpleCacheService mockCache;
    /** The MastodonServiceImpl instance under test. */
    private MastodonServiceImpl service;
    /** Jackson ObjectMapper used to construct test JSON payloads. */
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Initializes mocks and the service under test before each test.
     * @author Qiang Xue
     */
    @Before
    public void setUp() {
        mockClient = mock(MastodonClient.class);
        mockCache = mock(SimpleCacheService.class);
        service = new MastodonServiceImpl(mockClient, mockCache, pageSizeConfig(TEST_PAGE_SIZE));
    }

    /**
     * Builds a configuration holding the given mastodon.pageSize value.
     *
     * @param pageSize the configured page size
     * @return the parsed configuration
     * @author Qiang Xue
     */
    private static com.typesafe.config.Config pageSizeConfig(int pageSize) {
        return ConfigFactory.parseString("mastodon.pageSize = " + pageSize);
    }

    /**
     * Builds a service with an arbitrary HOCON configuration snippet.
     *
     * @param hocon the configuration text
     * @return a service using that configuration
     * @author Qiang Xue
     */
    private MastodonServiceImpl serviceWithConfig(String hocon) {
        return new MastodonServiceImpl(mockClient, mockCache, ConfigFactory.parseString(hocon));
    }

    /**
     * Tests that empty or null hashtag requests return an immediate error result without querying cache or API.
     * @author Qiang Xue
     */
    @Test
    public void testSearchHashtagWithEmptyOrNullInput() throws Exception {
        SearchResult r1 = service.searchHashtag(null, Optional.empty(), Optional.empty(), 1)
                .toCompletableFuture().get();
        assertTrue(r1.hasError());
        assertTrue(r1.getErrorMessage().orElse("").contains("empty"));

        SearchResult r2 = service.searchHashtag("   ", Optional.empty(), Optional.empty(), 1)
                .toCompletableFuture().get();
        assertTrue(r2.hasError());

        verifyNoInteractions(mockCache);
        verifyNoInteractions(mockClient);
    }

    /**
     * Tests cache hit behavior where cached SearchResult is returned directly.
     * @author Qiang Xue
     */
    @Test
    public void testSearchHashtagCacheHit() throws Exception {
        SearchResult cachedResult = new SearchResult("java", Collections.emptyList(), 1,
                Optional.empty(), Optional.empty(), 40);
        when(mockCache.<SearchResult>get(anyString())).thenReturn(Optional.of(cachedResult));

        SearchResult result = service.searchHashtag("java", Optional.empty(), Optional.empty(), 1)
                .toCompletableFuture().get();

        assertEquals("java", result.getHashtag());
        assertSame(cachedResult, result);
        verifyNoInteractions(mockClient);
    }

    /**
     * Tests cache miss behavior where remote client is queried, parsed, and cached.
     * @author Qiang Xue
     */
    @Test
    public void testSearchHashtagCacheMissSuccess() throws Exception {
        String jsonStr = "[" +
                "{\"id\":\"201\",\"created_at\":\"2026-10-25T10:00:00Z\",\"language\":\"en\"," +
                "\"content\":\"<p>Hello #java</p>\",\"url\":\"https://m.org/201\",\"visibility\":\"public\"," +
                "\"account\":{\"id\":\"1\",\"username\":\"dev\",\"display_name\":\"Developer\",\"url\":\"url\",\"avatar\":\"avatar\"}}," +
                "{\"id\":\"200\",\"created_at\":\"2026-10-25T09:00:00Z\",\"language\":\"fr\"," +
                "\"content\":\"<p>Bonjour</p>\",\"url\":\"https://m.org/200\",\"visibility\":\"public\"," +
                "\"account\":{\"id\":\"2\",\"username\":\"writer\",\"display_name\":\"Writer\",\"url\":\"profile\",\"avatar\":\"photo\"}}" +
                "]";
        JsonNode jsonNode = mapper.readTree(jsonStr);

        when(mockCache.<SearchResult>get(anyString())).thenReturn(Optional.empty());
        when(mockClient.getTagTimeline(eq("java"), eq(TEST_PAGE_SIZE), any(), any()))
                .thenReturn(CompletableFuture.completedFuture(jsonNode));

        SearchResult result = service.searchHashtag("#java", Optional.empty(), Optional.empty(), 1)
                .toCompletableFuture().get();

        assertFalse(result.hasError());
        assertEquals("java", result.getHashtag());
        assertEquals(2, result.getPosts().size());
        assertEquals("Hello #java", result.getPosts().get(0).getContent());
        assertEquals("1", result.getPosts().get(0).getAuthor().getId());
        assertEquals("dev", result.getPosts().get(0).getAuthor().getUsername());
        assertEquals("Developer", result.getPosts().get(0).getAuthor().getDisplayName());
        assertEquals("url", result.getPosts().get(0).getAuthor().getUrl());
        assertEquals("avatar", result.getPosts().get(0).getAuthor().getAvatar());
        assertEquals("200", result.getPosts().get(1).getId());
        assertEquals("writer", result.getPosts().get(1).getAuthor().getUsername());
        assertEquals(1, result.getPage());
        // The returned response has fewer than 40 posts, so there is no further page.
        assertFalse(result.hasNext());

        ArgumentCaptor<SearchResult> cachedResult = ArgumentCaptor.forClass(SearchResult.class);
        verify(mockCache).set(eq("tag:java:max_id:latest:min_id:latest:page:1"), cachedResult.capture(), eq(300L));
        assertSame(result, cachedResult.getValue());
        assertEquals(2, cachedResult.getValue().getPosts().size());
    }

    /**
     * Tests that the page anchors and cache key include both max_id and min_id, so that
     * "Previous" navigation (min_id) never collides with "Next" navigation (max_id).
     * @author Qiang Xue
     */
    @Test
    public void testSearchHashtagSecondPageAnchorsAndCacheKey() throws Exception {
        JsonNode jsonNode = mapper.readTree("[" +
                "{\"id\":\"80\"},{\"id\":\"70\"},{\"id\":\"60\"},{\"id\":\"50\"}]");

        when(mockCache.<SearchResult>get(anyString())).thenReturn(Optional.empty());
        when(mockClient.getTagTimeline(eq("java"), eq(TEST_PAGE_SIZE), eq(Optional.of("90")), eq(Optional.empty())))
                .thenReturn(CompletableFuture.completedFuture(jsonNode));

        SearchResult result = service.searchHashtag("java", Optional.of("90"), Optional.empty(), 2)
                .toCompletableFuture().get();

        assertEquals(2, result.getPage());
        assertEquals(Optional.of("90"), result.getPageMaxId());
        assertEquals(Optional.empty(), result.getPageMinId());
        assertTrue(result.hasPrevious());
        assertEquals("80", result.getPrevMinId().orElse(""));
        assertFalse(result.hasNext());

        verify(mockCache).set(eq("tag:java:max_id:90:min_id:latest:page:2"), any(SearchResult.class), eq(300L));
    }

    /**
     * Tests error handling when client call fails with an exception.
     * @author Qiang Xue
     */
    @Test
    public void testSearchHashtagRemoteClientException() throws Exception {
        when(mockCache.<SearchResult>get(anyString())).thenReturn(Optional.empty());
        when(mockClient.getTagTimeline(anyString(), anyInt(), any(), any()))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Connection refused")));

        SearchResult result = service.searchHashtag("offline", Optional.empty(), Optional.empty(), 1)
                .toCompletableFuture().get();

        assertTrue(result.hasError());
        assertTrue(result.getErrorMessage().orElse("").contains("Connection refused"));
    }

    /**
     * Tests parsing of various JSON structures (null, non-array, missing fields, empty language).
     * @author Qiang Xue
     * @author Owen Van Esbroeck
     */
    @Test
    public void testParsePostsEdgeCases() throws Exception {
        assertTrue(service.parsePosts(null).isEmpty());
        assertTrue(service.parsePosts(mapper.readTree("{\"not\":\"array\"}")).isEmpty());

        JsonNode arrayWithMissingFields = mapper.readTree("[{\"id\":\"1\"},{\"id\":\"2\",\"language\":\"\"},{\"id\":\"3\",\"language\":null}]");
        List<Post> posts = service.parsePosts(arrayWithMissingFields);
        assertEquals(3, posts.size());
        assertEquals("1", posts.get(0).getId());
        assertEquals("unknown", posts.get(0).getLanguage());
        assertEquals("2", posts.get(1).getId());
        assertEquals("unknown", posts.get(1).getLanguage());
        assertEquals("3", posts.get(2).getId());
        assertEquals("unknown", posts.get(2).getLanguage());
        assertEquals("", posts.get(0).getContent());
    }

    /**
     * Tests parsing of media attachments and a preview card from a status JSON object.
     *
     * @author Qiang Xue
     * @author Owen Van Esbroeck
     */
    @Test
    public void testParsePostsWithMediaAttachmentsAndCard() {
        ObjectNode status = mapper.createObjectNode();
        status.put("id", "300");
        status.put("created_at", "2026-01-01T00:00:00Z");
        status.put("language", "en");
        status.put("content", "<p>Hello</p>");
        status.put("url", "https://m.org/300");
        status.put("visibility", "public");
        status.put("replies_count", "1");
        status.put("reblogs_count", "2");
        status.put("favourites_count", "3");

        ObjectNode media = status.putArray("media_attachments").addObject();
        media.put("id", "m1");
        media.put("type", "video");
        media.put("url", "https://m.org/video.mp4");
        media.put("preview_url", "https://m.org/preview.jpg");
        media.put("description", "alt text");
        media.putObject("meta").putObject("small").put("width", "640").put("height", "480");

        ObjectNode card = status.putObject("card");
        card.put("url", "https://example.com");
        card.put("title", "Example");
        card.put("description", "Card description");
        card.put("type", "link");
        card.put("width", "100");
        card.put("height", "200");
        card.put("image", "https://example.com/image.png");

        status.putObject("account")
                .put("id", "1")
                .put("username", "alice")
                .put("display_name", "Alice")
                .put("url", "url")
                .put("avatar", "avatar");

        List<Post> posts = service.parsePosts(mapper.createArrayNode().add(status));

        assertEquals(1, posts.size());
        Post post = posts.get(0);
        assertEquals("300", post.getId());
        assertEquals(1, post.getAttachments().size());

        Attachment attachment = post.getAttachments().get(0);
        assertEquals("m1", attachment.getId());
        assertEquals("video", attachment.getType());
        assertEquals("https://m.org/video.mp4", attachment.getUrl());
        assertEquals("https://m.org/preview.jpg", attachment.getPreviewUrl());
        assertEquals("alt text", attachment.getDescription());
        assertEquals("640", attachment.getWidth());
        assertEquals("480", attachment.getHeight());

        assertTrue(post.hasCard());
        assertEquals("https://example.com", post.getCard().getUrl());
        assertEquals("Example", post.getCard().getTitle());
        assertEquals("Card description", post.getCard().getDescription());
        assertEquals("link", post.getCard().getType());
        assertEquals("100", post.getCard().getWidth());
        assertEquals("200", post.getCard().getHeight());
        assertEquals("https://example.com/image.png", post.getCard().getImage());
    }

    /**
     * Tests that an explicit null card is parsed as no card.
     *
     * @author Qiang Xue
     * @author Owen Van Esbroeck
     */
    @Test
    public void testParsePostsWithNullCard() {
        ObjectNode status = mapper.createObjectNode();
        status.put("id", "301");
        status.putNull("card");

        List<Post> posts = service.parsePosts(mapper.createArrayNode().add(status));

        assertEquals(1, posts.size());
        assertFalse(posts.get(0).hasCard());
    }

    /**
     * Tests that a search response assigns sad sentiment to the previously misclassified post.
     * @author Mihir Vipulbhai Panchal
     */
    @Test
    public void testSearchHashtagClassifiesScreenshotPostAsSad() throws Exception {
        String content = "I've noticed that AI loves to write `static` java functions. Put them everywhere. " +
                "I personally think this is crap, and it highlights the training data of Java code out " +
                "there isn't great. You can't swap static classes. You can test your code in isolation " +
                "with them. They're useful in certain circumstances. I think what's happening here is " +
                "knowledge vs wisdom - AI knows how to write Java code, but clearly hasn't lived through it. " +
                "#java #ai #claude #programming #tech #code";
        JsonNode json = mapper.createArrayNode().add(mapper.createObjectNode()
                .put("id", "1")
                .put("content", "<p>" + content + "</p>"))
                .add(mapper.createObjectNode()
                        .put("id", "2")
                        .put("content", "<p>I love this amazing update! 👍 :-)</p>"));
        when(mockCache.<SearchResult>get(anyString())).thenReturn(Optional.empty());
        when(mockClient.getTagTimeline(eq("java"), eq(TEST_PAGE_SIZE), eq(Optional.empty()), eq(Optional.empty())))
                .thenReturn(CompletableFuture.completedFuture(json));

        SearchResult result = service.searchHashtag("java", Optional.empty(), Optional.empty(), 1)
                .toCompletableFuture().get();

        assertFalse(result.hasError());
        assertEquals(2, result.getPosts().size());
        assertEquals(":-(", result.getPosts().get(0).getSentiment());
        assertEquals("I love this amazing update! 👍 :-)", result.getPosts().get(1).getContent());
        assertEquals(":-)", result.getPosts().get(1).getSentiment());
    }

    /**
     * Tests searchHashtag with null maxId/minId parameters.
     * @author Qiang Xue
     */
    @Test
    public void testSearchHashtagWithNullPaginationParameters() throws Exception {
        when(mockCache.<SearchResult>get(anyString())).thenReturn(Optional.empty());
        when(mockClient.getTagTimeline(eq("java"), eq(TEST_PAGE_SIZE), eq(Optional.empty()), eq(Optional.empty())))
                .thenReturn(CompletableFuture.completedFuture(mapper.readTree("[]")));

        SearchResult result = service.searchHashtag("java", null, null, 1).toCompletableFuture().get();
        assertFalse(result.hasError());
        assertTrue(result.getPosts().isEmpty());
        assertTrue(result.getNextMaxId().isEmpty());
        assertTrue(result.getPageMaxId().isEmpty());
        assertTrue(result.getPageMinId().isEmpty());
    }

    /**
     * Tests that the configured page size is used as the Mastodon limit parameter.
     * @author Qiang Xue
     */
    @Test
    public void testConfiguredPageSizeIsUsed() throws Exception {
        MastodonServiceImpl configured = serviceWithConfig("mastodon.pageSize = 12");
        when(mockCache.<SearchResult>get(anyString())).thenReturn(Optional.empty());
        when(mockClient.getTagTimeline(eq("java"), eq(12), eq(Optional.empty()), eq(Optional.empty())))
                .thenReturn(CompletableFuture.completedFuture(mapper.readTree("[]")));

        configured.searchHashtag("java", Optional.empty(), Optional.empty(), 1).toCompletableFuture().get();

        assertEquals(12, configured.getPageSize());
        verify(mockClient).getTagTimeline("java", 12, Optional.empty(), Optional.empty());
    }

    /**
     * Tests that a full page according to the configured page size offers a next page.
     * @author Qiang Xue
     */
    @Test
    public void testConfiguredPageSizeDrivesHasNext() throws Exception {
        MastodonServiceImpl configured = serviceWithConfig("mastodon.pageSize = 2");
        when(mockCache.<SearchResult>get(anyString())).thenReturn(Optional.empty());
        when(mockClient.getTagTimeline(eq("java"), eq(2), any(), any()))
                .thenReturn(CompletableFuture.completedFuture(mapper.readTree("[{\"id\":\"2\"},{\"id\":\"1\"}]")));

        SearchResult result = configured.searchHashtag("java", Optional.empty(), Optional.empty(), 1)
                .toCompletableFuture().get();

        assertTrue(result.hasNext());
        assertEquals("1", result.getNextMaxId().orElse(""));
    }

    /**
     * Tests that a missing mastodon.pageSize key falls back to the default page size.
     * @author Qiang Xue
     */
    @Test
    public void testPageSizeDefaultsWhenKeyIsMissing() {
        MastodonServiceImpl unconfigured =
                new MastodonServiceImpl(mockClient, mockCache, ConfigFactory.empty());

        assertEquals(MastodonServiceImpl.DEFAULT_PAGE_SIZE, unconfigured.getPageSize());
    }

    /**
     * Tests that a null configuration falls back to the default page size.
     * @author Qiang Xue
     */
    @Test
    public void testPageSizeDefaultsWhenConfigIsNull() {
        MastodonServiceImpl unconfigured = new MastodonServiceImpl(mockClient, mockCache, null);

        assertEquals(MastodonServiceImpl.DEFAULT_PAGE_SIZE, unconfigured.getPageSize());
    }

    /**
     * Tests that zero and negative page sizes fall back to the default page size.
     * @author Qiang Xue
     */
    @Test
    public void testNonPositivePageSizeFallsBackToDefault() {
        assertEquals(MastodonServiceImpl.DEFAULT_PAGE_SIZE,
                serviceWithConfig("mastodon.pageSize = 0").getPageSize());
        assertEquals(MastodonServiceImpl.DEFAULT_PAGE_SIZE,
                serviceWithConfig("mastodon.pageSize = -7").getPageSize());
    }

    /**
     * Tests that page sizes above the Mastodon API maximum are capped at 40.
     * @author Qiang Xue
     */
    @Test
    public void testPageSizeIsCappedAtApiMaximum() {
        MastodonServiceImpl configured = serviceWithConfig("mastodon.pageSize = 500");

        assertEquals(MastodonServiceImpl.MAX_PAGE_SIZE, configured.getPageSize());
        assertEquals(40, configured.getPageSize());
    }

    /**
     * Tests that a non-numeric page size falls back to the default page size.
     * @author Qiang Xue
     */
    @Test
    public void testNonNumericPageSizeFallsBackToDefault() {
        MastodonServiceImpl misconfigured = serviceWithConfig("mastodon.pageSize = \"many\"");

        assertEquals(MastodonServiceImpl.DEFAULT_PAGE_SIZE, misconfigured.getPageSize());
    }
}
