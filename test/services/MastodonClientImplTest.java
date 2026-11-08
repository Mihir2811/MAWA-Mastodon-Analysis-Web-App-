package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.Config;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MastodonClientImpl, mocking java.net.http.HttpClient to verify request building,
 * headers, URI query parameters, and response handling without live network calls.
 *
 * @author Qiang Xue
 */
public class MastodonClientImplTest {

    /** Mock HttpClient used to intercept HTTP calls without network activity. */
    private HttpClient mockHttpClient;
    /** Mock HttpResponse returning mocked status codes and payloads. */
    private HttpResponse<String> mockResponse;
    /** Mock Config providing application settings. */
    private Config mockConfig;

    /**
     * Sets up mock dependencies before each test.
     * @author Qiang Xue
     */
    @Before
    @SuppressWarnings("unchecked")
    public void setUp() {
        mockHttpClient = mock(HttpClient.class);
        mockResponse = (HttpResponse<String>) mock(HttpResponse.class);
        mockConfig = mock(Config.class);

        when(mockConfig.hasPath("mastodon.instance")).thenReturn(true);
        when(mockConfig.getString("mastodon.instance")).thenReturn("mastodon.social");
        when(mockConfig.hasPath("mastodon.token")).thenReturn(true);
        when(mockConfig.getString("mastodon.token")).thenReturn("test-token");
    }

    /**
     * Tests request construction for a successful 200 OK response with next-page pagination.
     * @author Qiang Xue
     */
    @Test
    public void testGetTagTimelineSuccess() throws Exception {
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn("[{\"id\":\"101\"}]");
        doReturn(CompletableFuture.completedFuture(mockResponse))
                .when(mockHttpClient).sendAsync(any(HttpRequest.class), any());

        MastodonClientImpl client = new MastodonClientImpl(mockConfig, mockHttpClient);
        CompletionStage<JsonNode> stage = client.getTagTimeline("#java", 20, Optional.of("999"), Optional.empty());

        JsonNode result = stage.toCompletableFuture().get();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("101", result.get(0).get("id").asText());

        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(mockHttpClient).sendAsync(requestCaptor.capture(), any());
        HttpRequest request = requestCaptor.getValue();

        assertEquals("https://mastodon.social/api/v1/timelines/tag/java?limit=20&max_id=999", request.uri().toString());
        assertTrue(request.headers().firstValue("Authorization").isPresent());
        assertEquals("Bearer test-token", request.headers().firstValue("Authorization").get());
    }

    /**
     * Tests that min_id is used to request the previous (newer) page.
     * @author Qiang Xue
     */
    @Test
    public void testGetTagTimelineWithPreviousPageMinId() throws Exception {
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn("[]");
        doReturn(CompletableFuture.completedFuture(mockResponse))
                .when(mockHttpClient).sendAsync(any(HttpRequest.class), any());

        MastodonClientImpl client = new MastodonClientImpl(mockConfig, mockHttpClient);
        client.getTagTimeline("java", 40, Optional.empty(), Optional.of("500")).toCompletableFuture().get();

        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(mockHttpClient).sendAsync(requestCaptor.capture(), any());

        assertEquals("https://mastodon.social/api/v1/timelines/tag/java?limit=40&min_id=500",
                requestCaptor.getValue().uri().toString());
    }

    /**
     * Tests fallback to default instance when configured instance is whitespace, and empty token behavior.
     * @author Qiang Xue
     */
    @Test
    public void testWhitespaceInstanceAndEmptyToken() throws Exception {
        Config emptyTokenConfig = mock(Config.class);
        when(emptyTokenConfig.hasPath("mastodon.instance")).thenReturn(true);
        when(emptyTokenConfig.getString("mastodon.instance")).thenReturn("   ");
        when(emptyTokenConfig.hasPath("mastodon.token")).thenReturn(true);
        when(emptyTokenConfig.getString("mastodon.token")).thenReturn("");

        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn("[]");
        doReturn(CompletableFuture.completedFuture(mockResponse))
                .when(mockHttpClient).sendAsync(any(HttpRequest.class), any());

        MastodonClientImpl client = new MastodonClientImpl(emptyTokenConfig, mockHttpClient);
        assertEquals("mastodon.social", client.getInstance());

        // Test with null pagination parameters
        client.getTagTimeline("test", 10, null, null).toCompletableFuture().get();

        // Test with whitespace-only pagination parameters
        client.getTagTimeline("test", 10, Optional.of("   "), Optional.of("  ")).toCompletableFuture().get();

        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(mockHttpClient, times(2)).sendAsync(requestCaptor.capture(), any());
        HttpRequest request = requestCaptor.getAllValues().get(1);

        assertFalse(request.headers().firstValue("Authorization").isPresent());
        assertEquals("https://mastodon.social/api/v1/timelines/tag/test?limit=10", request.uri().toString());
    }

    /**
     * Tests fallback to default instance and empty token when configuration paths are missing.
     * @author Qiang Xue
     */
    @Test
    public void testDefaultConfigurationFallback() {
        Config emptyConfig = mock(Config.class);
        when(emptyConfig.hasPath("mastodon.instance")).thenReturn(false);
        when(emptyConfig.hasPath("mastodon.token")).thenReturn(false);

        MastodonClientImpl client = new MastodonClientImpl(emptyConfig, mockHttpClient);
        assertEquals("mastodon.social", client.getInstance());
    }

    /**
     * Tests production constructor with single Config parameter.
     * @author Qiang Xue
     */
    @Test
    public void testSingleConfigConstructor() {
        MastodonClientImpl client = new MastodonClientImpl(mockConfig);
        assertEquals("mastodon.social", client.getInstance());
    }

    /**
     * Tests that characters which are significant inside a URL are percent-encoded, so that the
     * query string is not swallowed by a fragment (a raw '#' would hide {@code ?limit=...} and
     * make Mastodon fall back to its own default page size).
     * @author Qiang Xue
     */
    @Test
    public void testHashtagIsUrlEncoded() throws Exception {
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn("[]");
        doReturn(CompletableFuture.completedFuture(mockResponse))
                .when(mockHttpClient).sendAsync(any(HttpRequest.class), any());

        MastodonClientImpl client = new MastodonClientImpl(mockConfig, mockHttpClient);
        client.getTagTimeline("c#", 1, Optional.empty(), Optional.empty()).toCompletableFuture().get();
        client.getTagTimeline("café", 1, Optional.empty(), Optional.empty()).toCompletableFuture().get();

        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(mockHttpClient, times(2)).sendAsync(requestCaptor.capture(), any());

        HttpRequest hashRequest = requestCaptor.getAllValues().get(0);
        assertEquals("https://mastodon.social/api/v1/timelines/tag/c%23?limit=1", hashRequest.uri().toString());
        assertEquals("/api/v1/timelines/tag/c%23", hashRequest.uri().getRawPath());
        assertEquals("limit=1", hashRequest.uri().getRawQuery());

        HttpRequest accentedRequest = requestCaptor.getAllValues().get(1);
        assertEquals("https://mastodon.social/api/v1/timelines/tag/caf%C3%A9?limit=1",
                accentedRequest.uri().toString());
    }

    /**
     * Tests error handling when Mastodon API returns a non-200 HTTP status code.
     * @author Qiang Xue
     */
    @Test
    public void testGetTagTimelineHttpError() {
        when(mockResponse.statusCode()).thenReturn(500);
        when(mockResponse.body()).thenReturn("Internal Server Error");
        doReturn(CompletableFuture.completedFuture(mockResponse))
                .when(mockHttpClient).sendAsync(any(HttpRequest.class), any());

        MastodonClientImpl client = new MastodonClientImpl(mockConfig, mockHttpClient);
        CompletionStage<JsonNode> stage = client.getTagTimeline("java", 10, Optional.empty(), Optional.empty());

        try {
            stage.toCompletableFuture().get();
            fail("Expected exception when API returns status 500");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("500"));
        }
    }
}
