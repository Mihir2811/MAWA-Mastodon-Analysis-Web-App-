package services;

import models.Account;
import models.Post;
import models.WordStat;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for WordStatsServiceImpl using Java Streams API.
 *
 * @author Qiang Xue
 */
public class WordStatsServiceImplTest {

    /** The WordStatsServiceImpl instance under test. */
    private WordStatsServiceImpl service;

    /**
     * Initializes service before each test.
     *
     * @author Qiang Xue
     */
    @Before
    public void setUp() {
        service = new WordStatsServiceImpl();
    }

    /**
     * Tests computeWordStats with null or empty list of posts.
     *
     * @author Qiang Xue
     */
    @Test
    public void testComputeWordStatsWithNullOrEmpty() {
        assertTrue(service.computeWordStats(null).isEmpty());
        assertTrue(service.computeWordStats(Collections.emptyList()).isEmpty());
    }

    /**
     * Tests filtering of null posts and posts with empty or whitespace content.
     *
     * @author Qiang Xue
     */
    @Test
    public void testComputeWordStatsWithBlankOrNullPosts() {
        Account author = new Account("1", "user", "User", "url", "avatar");
        Post nullPost = null;
        Post emptyContentPost = new Post("1", author, "t", "en", "", "", "url", "public");
        Post whitespacePost = new Post("2", author, "t", "en", "   \t\n  ", "", "url", "public");
        Post nullContentPost = new Post("3", author, "t", "en", null, "", "url", "public");

        List<WordStat> stats = service.computeWordStats(Arrays.asList(nullPost, emptyContentPost, whitespacePost, nullContentPost));
        assertTrue(stats.isEmpty());
    }

    /**
     * Tests word counting, punctuation removal, case-insensitivity, and descending order.
     *
     * @author Qiang Xue
     */
    @Test
    public void testComputeWordStatsDescendingFrequencyAndOrdering() {
        Account author = new Account("1", "user", "User", "url", "avatar");
        Post p1 = new Post("1", author, "t", "en", "Java is great! Java, Java.", "", "url", "public");
        Post p2 = new Post("2", author, "t", "en", "Play framework is great; play is fun.", "", "url", "public");

        // Frequencies:
        // java: 3
        // is: 3 (tie-breaking with java -> alphabetical 'is' vs 'java' -> 'is' comes before 'java')
        // great: 2
        // play: 2 (tie-breaking with great -> alphabetical 'great' comes before 'play')
        // framework: 1
        // fun: 1 (tie-breaking with framework -> 'framework' comes before 'fun')

        List<WordStat> stats = service.computeWordStats(Arrays.asList(p1, p2));

        assertEquals(6, stats.size());

        assertEquals(new WordStat("is", 3), stats.get(0));
        assertEquals(new WordStat("java", 3), stats.get(1));
        assertEquals(new WordStat("great", 2), stats.get(2));
        assertEquals(new WordStat("play", 2), stats.get(3));
        assertEquals(new WordStat("framework", 1), stats.get(4));
        assertEquals(new WordStat("fun", 1), stats.get(5));
    }

    /**
     * Tests handling of Unicode characters and digits.
     *
     * @author Qiang Xue
     */
    @Test
    public void testUnicodeAndDigits() {
        Account author = new Account("1", "user", "User", "url", "avatar");
        Post p = new Post("1", author, "t", "fr", "Café 100% délicieux! Café.", "", "url", "public");

        List<WordStat> stats = service.computeWordStats(Collections.singletonList(p));

        assertEquals(3, stats.size());
        assertEquals("café", stats.get(0).getWord());
        assertEquals(2, stats.get(0).getCount());
    }
}
