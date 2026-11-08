package models;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for WordStat domain model.
 *
 * @author Qiang Xue
 */
public class WordStatTest {

    /**
     * Tests standard creation and getters.
     *
     * @author Qiang Xue
     */
    @Test
    public void testCreationAndGetters() {
        WordStat stat = new WordStat("play", 42);
        assertEquals("play", stat.getWord());
        assertEquals(42, stat.getCount());
    }

    /**
     * Tests null word fallback to empty string.
     *
     * @author Qiang Xue
     */
    @Test
    public void testNullWordFallback() {
        WordStat stat = new WordStat(null, 5);
        assertEquals("", stat.getWord());
        assertEquals(5, stat.getCount());
    }

    /**
     * Tests equals and hashCode across matching and non-matching instances.
     *
     * @author Qiang Xue
     */
    @Test
    public void testEqualsAndHashCode() {
        WordStat s1 = new WordStat("scala", 10);
        WordStat s2 = new WordStat("scala", 10);
        WordStat sDiffWord = new WordStat("java", 10);
        WordStat sDiffCount = new WordStat("scala", 15);

        assertEquals(s1, s1);
        assertNotEquals(s1, null);
        assertNotEquals(s1, "other type");

        assertEquals(s1, s2);
        assertEquals(s1.hashCode(), s2.hashCode());

        assertNotEquals(s1, sDiffWord);
        assertNotEquals(s1, sDiffCount);
    }

    /**
     * Tests toString format.
     *
     * @author Qiang Xue
     */
    @Test
    public void testToString() {
        WordStat stat = new WordStat("framework", 3);
        String str = stat.toString();
        assertTrue(str.contains("framework"));
        assertTrue(str.contains("3"));
    }
}
