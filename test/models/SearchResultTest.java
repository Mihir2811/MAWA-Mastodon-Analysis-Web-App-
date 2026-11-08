package models;

import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

/**
 * Unit tests for SearchResult model, validating success and error constructors, getters,
 * state predicates, and the derived pagination anchors used by [ Previous ] / [ Next ].
 *
 * @author Qiang Xue
 */
public class SearchResultTest {

    /**
     * Builds a post with the given id for pagination tests.
     *
     * @param id the post id
     * @return a Post instance
     * @author Qiang Xue
     */
    private Post post(String id) {
        Account author = new Account("1", "alice", "Alice", "url", "avatar");
        return new Post(id, author, "time", "en", "hello", "<p>hello</p>", "url", "public");
    }

    /**
     * Tests SearchResult construction for a successful query with posts and pagination token.
     * @author Qiang Xue
     */
    @Test
    public void testSuccessfulSearchResult() {
        List<Post> posts = List.of(post("1001"));

        SearchResult result = new SearchResult("java", posts, 1, Optional.empty(), Optional.empty(), 40);

        assertEquals("java", result.getHashtag());
        assertEquals(1, result.getPosts().size());
        assertEquals(1, result.getPage());
        assertFalse(result.hasError());
        assertFalse(result.isEmpty());
        assertTrue(result.getErrorMessage().isEmpty());
        // A short page means there is no further (older) page.
        assertFalse(result.hasNext());
        assertTrue(result.getNextMaxId().isEmpty());
        // Page 1 never offers a Previous link.
        assertFalse(result.hasPrevious());
    }

    /**
     * Tests the pagination anchors derived for a full page of results.
     * @author Qiang Xue
     */
    @Test
    public void testPaginationAnchorsForFullPage() {
        List<Post> posts = List.of(post("30"), post("20"), post("10"));

        SearchResult result = new SearchResult("java", posts, 2, Optional.of("40"), Optional.empty(), 3);

        assertTrue(result.hasNext());
        assertEquals("10", result.getNextMaxId().orElse(""));
        assertTrue(result.hasPrevious());
        assertEquals("30", result.getPrevMinId().orElse(""));
        assertEquals(Optional.of("40"), result.getPageMaxId());
        assertEquals(Optional.empty(), result.getPageMinId());
    }

    /**
     * Tests that the first page never offers a Previous link, even for a full page.
     * @author Qiang Xue
     */
    @Test
    public void testFirstPageHasNoPrevious() {
        List<Post> posts = List.of(post("30"), post("20"), post("10"));

        SearchResult result = new SearchResult("java", posts, 1, Optional.empty(), Optional.empty(), 3);

        assertTrue(result.hasNext());
        assertFalse(result.hasPrevious());
        assertTrue(result.getPrevMinId().isEmpty());
    }

    /**
     * Tests SearchResult construction for an error scenario.
     * @author Qiang Xue
     */
    @Test
    public void testErrorSearchResult() {
        SearchResult result = new SearchResult("errorTag", "API rate limited");

        assertEquals("errorTag", result.getHashtag());
        assertTrue(result.getPosts().isEmpty());
        assertEquals(1, result.getPage());
        assertTrue(result.getNextMaxId().isEmpty());
        assertTrue(result.getPrevMinId().isEmpty());
        assertFalse(result.hasNext());
        assertFalse(result.hasPrevious());
        assertTrue(result.hasError());
        assertTrue(result.isEmpty());
        assertEquals("API rate limited", result.getErrorMessage().orElse(""));
    }

    /**
     * Tests SearchResult construction with null parameters for safe defaults.
     * @author Qiang Xue
     */
    @Test
    public void testSearchResultWithNulls() {
        SearchResult successNulls = new SearchResult(null, null, 0, null, null, 40);
        assertEquals("", successNulls.getHashtag());
        assertTrue(successNulls.getPosts().isEmpty());
        assertEquals(1, successNulls.getPage());
        assertTrue(successNulls.getNextMaxId().isEmpty());
        assertTrue(successNulls.getPageMaxId().isEmpty());
        assertTrue(successNulls.getPageMinId().isEmpty());
        assertFalse(successNulls.hasError());
        assertTrue(successNulls.isEmpty());

        SearchResult errorNulls = new SearchResult(null, (String) null);
        assertEquals("", errorNulls.getHashtag());
        assertFalse(errorNulls.hasError());
        assertTrue(errorNulls.getErrorMessage().isEmpty());
    }

    /**
     * Tests that an empty page keeps the supplied pagination parameters and offers no navigation.
     * @author Qiang Xue
     */
    @Test
    public void testEmptyPageKeepsPaginationParameters() {
        SearchResult result = new SearchResult("java", Collections.emptyList(), 3,
                Optional.of("99"), Optional.empty(), 40);

        assertTrue(result.isEmpty());
        assertEquals(3, result.getPage());
        assertEquals(Optional.of("99"), result.getPageMaxId());
        assertFalse(result.hasNext());
        assertFalse(result.hasPrevious());
    }

    /**
     * Tests that a non-positive page size never reports a next page.
     * @author Qiang Xue
     */
    @Test
    public void testNonPositivePageSizeHasNoNextPage() {
        SearchResult result = new SearchResult("java", List.of(post("1001")), 1,
                Optional.empty(), Optional.empty(), 0);

        assertFalse(result.hasNext());
        assertTrue(result.getNextMaxId().isEmpty());
    }
}
