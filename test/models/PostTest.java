package models;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for Post domain model, validating constructors, getters, equals, hashCode, and toString methods.
 *
 * @author Kush Chaudhari
 * @author Mihir Vipulbhai Panchal
 * @author Owen Van Esbroeck
 * @author Qiang Xue
 */
public class PostTest {

    /**
     * Tests post creation with standard values.
     * @author Qiang Xue
     */
    @Test
    public void testPostCreationWithStandardValues() {
        Account author = new Account("1", "alice", "Alice", "url", "avatar");
        Post post = new Post("1001", author, "2026-10-25T12:00:00Z", "en", "Hello World", "<p>Hello World</p>", "https://example.com/post/1001", "public");

        assertEquals("1001", post.getId());
        assertEquals(author, post.getAuthor());
        assertEquals("2026-10-25T12:00:00Z", post.getCreatedAt());
        assertEquals("en", post.getLanguage());
        assertEquals("Hello World", post.getContent());
        assertEquals("<p>Hello World</p>", post.getRawContent());
        assertEquals("https://example.com/post/1001", post.getUrl());
        assertEquals("public", post.getVisibility());
    }

    /**
     * Tests post creation with null values to verify safe fallback defaults.
     * @author Qiang Xue
     */
    @Test
    public void testPostCreationWithNullValues() {
        Post post = new Post(null, null, null, null, null, null, null, null);

        assertEquals("", post.getId());
        assertNull(post.getAuthor());
        assertEquals("", post.getCreatedAt());
        assertEquals("unknown", post.getLanguage());
        assertEquals("", post.getContent());
        assertEquals("", post.getRawContent());
        assertEquals("", post.getUrl());
        assertEquals("public", post.getVisibility());
    }

    /**
     * Tests equals and hashCode contract across varying attributes.
     * @author Qiang Xue
     */
    @Test
    public void testPostEqualsAndHashCode() {
        Account author1 = new Account("1", "alice", "Alice", "url", "avatar");
        Account author2 = new Account("2", "bob", "Bob", "url", "avatar");

        Post p1 = new Post("1", author1, "time", "en", "content", "raw", "url", "public");
        Post p2 = new Post("1", author1, "time", "en", "content", "raw", "url", "public");

        // Reflexivity & null / type check
        assertEquals(p1, p1);
        assertNotEquals(p1, null);
        assertNotEquals(p1, new Object());

        // Equality
        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());

        // Individual field variations
        assertNotEquals(p1, new Post("2", author1, "time", "en", "content", "raw", "url", "public"));
        assertNotEquals(p1, new Post("1", author2, "time", "en", "content", "raw", "url", "public"));
        assertNotEquals(p1, new Post("1", author1, "other_time", "en", "content", "raw", "url", "public"));
        assertNotEquals(p1, new Post("1", author1, "time", "fr", "content", "raw", "url", "public"));
        assertNotEquals(p1, new Post("1", author1, "time", "en", "diff_content", "raw", "url", "public"));
        assertNotEquals(p1, new Post("1", author1, "time", "en", "content", "diff_raw", "url", "public"));
        assertNotEquals(p1, new Post("1", author1, "time", "en", "content", "raw", "diff_url", "public"));
        assertNotEquals(p1, new Post("1", author1, "time", "en", "content", "raw", "url", "unlisted"));
        assertNotEquals(p1, new Post("1", author1, "time", "en", "content", "raw", "url", "public",
                null, null, null, ":-)", null, null));
    }

    /**
     * Tests equality for reply, reblog and favourite counters and the card-presence check.
     *
     * @author Qiang Xue
     */
    @Test
    public void testPostEqualsEngagementFieldsAndHasCard() {
        Account author = new Account("1", "alice", "Alice", "url", "avatar");
        Card card = new Card("card-url", "title", "description", "link", "100", "200", "image");
        Post base = new Post("1", author, "time", "en", "content", "raw", "url", "public",
                "1", "2", "3", ":-)", List.of(), card);
        Post same = new Post("1", author, "time", "en", "content", "raw", "url", "public",
                "1", "2", "3", ":-)", List.of(), card);

        assertEquals(base, same);
        assertEquals(base.hashCode(), same.hashCode());
        assertNotEquals(base, new Post("1", author, "time", "en", "content", "raw", "url", "public",
                "9", "2", "3", ":-)", List.of(), card));
        assertNotEquals(base, new Post("1", author, "time", "en", "content", "raw", "url", "public",
                "1", "9", "3", ":-)", List.of(), card));
        assertNotEquals(base, new Post("1", author, "time", "en", "content", "raw", "url", "public",
                "1", "2", "9", ":-)", List.of(), card));
        assertTrue(base.hasCard());
        assertFalse(new Post("2", author, "time", "en", "content", "raw", "url", "public").hasCard());
    }

    /**
     * Tests toString format with author and without author.
     * @author Qiang Xue
     */
    @Test
    public void testPostToString() {
        Account author = new Account("1", "alice", "Alice", "url", "avatar");
        Post pWithAuthor = new Post("1001", author, "time", "en", "content", "raw", "url", "public");
        assertTrue(pWithAuthor.toString().contains("alice"));
        assertTrue(pWithAuthor.toString().contains("1001"));

        Post pNoAuthor = new Post("1002", null, "time", "en", "content", "raw", "url", "public");
        assertTrue(pNoAuthor.toString().contains("null"));
        assertTrue(pNoAuthor.toString().contains("1002"));
    }
}
