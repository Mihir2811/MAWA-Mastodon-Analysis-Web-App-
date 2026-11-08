package services;

import models.Account;
import models.AuthorStat;
import models.Post;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Unit tests for {@link AuthorAnalysisServiceImpl}, verifying post counting, author deduplication,
 * tie-break sorting, and language breakdown logic.
 *
 * @author Kush Chaudhari
 */
public class AuthorAnalysisServiceImplTest {

    /** The AuthorAnalysisServiceImpl instance under test. */
    private final AuthorAnalysisServiceImpl service = new AuthorAnalysisServiceImpl();

    /**
     * Tests counting posts per author account ID, aggregating language breakdown,
     * and sorting authors primarily by post count and secondary by display name.
     *
     * @author Kush Chaudhari
     */
    @Test
    public void countsPostsByAccountIdAndSortsByCountThenDisplayName() {
        Account bob = new Account("2", "bob", "Bob", "https://example.com/bob", "bob-avatar");
        Account alice = new Account("1", "alice", "Alice", "https://example.com/alice", "alice-avatar");
        List<Post> posts = Arrays.asList(
                post("1", bob),
                post("2", alice, "en"),
                post("3", bob),
                post("4", alice, "fr"),
                post("5", new Account("1", "alice", "Alice", "https://example.com/alice", "alice-avatar"), "en"));

        List<AuthorStat> stats = service.computeAuthorStats(posts);

        assertEquals(2, stats.size());
        assertEquals("alice", stats.get(0).getAuthor().getUsername());
        assertEquals(3L, stats.get(0).getPostCount());
        assertEquals("2", stats.get(0).getLatestPost().getId());
        assertEquals(2, stats.get(0).getLanguageCounts().size());
        assertEquals(Long.valueOf(2), stats.get(0).getLanguageCounts().get("en"));
        assertEquals(Long.valueOf(1), stats.get(0).getLanguageCounts().get("fr"));
        assertEquals("bob", stats.get(1).getAuthor().getUsername());
        assertEquals(2L, stats.get(1).getPostCount());
    }

    /**
     * Tests that accounts with distinct IDs remain separate even when their usernames match.
     *
     * @author Kush Chaudhari
     */
    @Test
    public void keepsAccountsWithDifferentIdsSeparateWhenUsernamesMatch() {
        List<AuthorStat> stats = service.computeAuthorStats(Arrays.asList(
                post("1", new Account("account-1", "shared", "Shared", "", "")),
                post("2", new Account("account-2", "shared", "Shared", "", ""))));

        assertEquals(2, stats.size());
        assertEquals(1L, stats.get(0).getPostCount());
        assertEquals(1L, stats.get(1).getPostCount());
    }

    /**
     * Tests that null, empty lists, or posts without author accounts return empty results gracefully.
     *
     * @author Kush Chaudhari
     */
    @Test
    public void handlesEmptyPostsAndPostsWithoutAccountData() {
        assertEquals(Collections.emptyList(), service.computeAuthorStats(null));
        assertEquals(Collections.emptyList(), service.computeAuthorStats(Collections.emptyList()));
        assertEquals(Collections.emptyList(), service.computeAuthorStats(Collections.singletonList(post("1", null))));
    }

    /**
     * Tests fallback grouping by username when account IDs are missing.
     *
     * @author Kush Chaudhari
     */
    @Test
    public void groupsAuthorsByUsernameWhenAccountIdIsMissing() {
        Account author = new Account("", "guest", "Guest", "", "");

        List<AuthorStat> stats = service.computeAuthorStats(Arrays.asList(
                post("1", author),
                post("2", new Account("", "guest", "Guest", "", ""))));

        assertEquals(1, stats.size());
        assertEquals(2L, stats.get(0).getPostCount());
    }

    /**
     * Tests that username fallback grouping ignores case when account IDs are missing.
     *
     * @author Kush Chaudhari
     */
    @Test
    public void groupsAuthorsByUsernameIgnoringCaseWhenAccountIdIsMissing() {
        List<AuthorStat> stats = service.computeAuthorStats(Arrays.asList(
                post("1", new Account("", "Alice", "Alice", "", "")),
                post("2", new Account("", "alice", "Alice", "", ""))));

        assertEquals(1, stats.size());
        assertEquals(2L, stats.get(0).getPostCount());
    }

    /**
     * Tests alphabetical tie-breaking when two authors have identical post counts.
     *
     * @author Kush Chaudhari
     */
    @Test
    public void sortsAuthorsAlphabeticallyWhenPostCountsTie() {
        List<AuthorStat> stats = service.computeAuthorStats(Arrays.asList(
                post("1", new Account("2", "bob", "Bob", "", "")),
                post("2", new Account("1", "alice", "Alice", "", ""))));

        assertEquals("alice", stats.get(0).getAuthor().getUsername());
        assertEquals("bob", stats.get(1).getAuthor().getUsername());
    }

    /**
     * Tests username tie-breaking when authors have the same display name and post count.
     *
     * @author Kush Chaudhari
     */
    @Test
    public void sortsAuthorsByUsernameWhenDisplayNamesAndPostCountsTie() {
        List<AuthorStat> stats = service.computeAuthorStats(Arrays.asList(
                post("1", new Account("2", "zoe", "Same Name", "", "")),
                post("2", new Account("1", "amy", "Same Name", "", ""))));

        assertEquals("amy", stats.get(0).getAuthor().getUsername());
        assertEquals("zoe", stats.get(1).getAuthor().getUsername());
    }

    /**
     * Tests that the first post is retained as newest when input posts are newest-first.
     *
     * @author Kush Chaudhari
     */
    @Test
    public void keepsFirstPostAsLatestWhenPostsAreNewestFirst() {
        Account author = new Account("1", "alice", "Alice", "", "");
        List<Post> posts = Arrays.asList(
                new Post("newest", author, "2026-11-08T12:00:00Z", "en", "", "", "", "public"),
                new Post("older", author, "2026-11-08T11:00:00Z", "en", "", "", "", "public"));

        List<AuthorStat> stats = service.computeAuthorStats(posts);

        assertEquals("newest", stats.get(0).getLatestPost().getId());
    }

    /**
     * Tests that returned author statistics and language counts cannot be modified.
     *
     * @author Kush Chaudhari
     */
    @Test
    public void returnsUnmodifiableStatsAndLanguageCounts() {
        List<AuthorStat> stats = service.computeAuthorStats(
                Collections.singletonList(post("1", new Account("1", "alice", "Alice", "", ""))));

        try {
            stats.add(stats.get(0));
            fail("Expected the author statistics list to be unmodifiable");
        } catch (UnsupportedOperationException expected) {
            // Expected: callers must not be able to modify computed results.
        }

        try {
            stats.get(0).getLanguageCounts().put("fr", 1L);
            fail("Expected the language counts map to be unmodifiable");
        } catch (UnsupportedOperationException expected) {
            // Expected: callers must not be able to modify computed results.
        }
    }

    /**
     * Tests null list entries, authors without identifiers, and URL-based author keys.
     *
     * @author Kush Chaudhari
     */
    @Test
    public void handlesNullPostsAndAuthorsWithoutIdsOrUsernames() {
        Account urlOnly = new Account("", "", "", "https://example.com/urlonly", "");
        Account noIdentifier = new Account("", "", "", "", "");
        List<Post> posts = Arrays.asList(
                null,
                post("1", noIdentifier),
                post("2", urlOnly),
                post("3", new Account("", "", "", "https://example.com/urlonly", "")));

        List<AuthorStat> stats = service.computeAuthorStats(posts);

        assertEquals(1, stats.size());
        assertEquals("https://example.com/urlonly", stats.get(0).getAuthor().getUrl());
        assertEquals(2L, stats.get(0).getPostCount());
    }

    /**
     * Helper method to construct a post with default English language for test fixtures.
     *
     * @param id     the post identifier
     * @param author the author account
     * @return a Post instance
     * @author Kush Chaudhari
     */
    private static Post post(String id, Account author) {
        return post(id, author, "en");
    }

    /**
     * Helper method to construct a post with custom language for test fixtures.
     *
     * @param id       the post identifier
     * @param author   the author account
     * @param language the post language code
     * @return a Post instance
     * @author Kush Chaudhari
     */
    private static Post post(String id, Account author, String language) {
        return new Post(id, author, "", language, "", "", "", "public");
    }
}
