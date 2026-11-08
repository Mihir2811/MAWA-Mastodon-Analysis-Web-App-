package models;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for the Card domain model, covering construction, getters,
 * equals/hashCode and toString.
 *
 * @author Owen Van Esbroeck
 */
public class CardTest {

    /**
     * Tests construction with non-null values and all getters.
     *
     * @author Owen Van Esbroeck
     */
    @Test
    public void testConstructorAndGettersWithValues() {
        Card card = new Card("https://example.com", "Example", "A description",
                "link", "100", "200", "https://example.com/image.png");

        assertEquals("https://example.com", card.getUrl());
        assertEquals("Example", card.getTitle());
        assertEquals("A description", card.getDescription());
        assertEquals("link", card.getType());
        assertEquals("100", card.getWidth());
        assertEquals("200", card.getHeight());
        assertEquals("https://example.com/image.png", card.getImage());
    }

    /**
     * Tests the null-safe defaults applied by the constructor for every parameter.
     *
     * @author Owen Van Esbroeck
     */
    @Test
    public void testConstructorWithNullValuesUsesDefaults() {
        Card card = new Card(null, null, null, null, null, null, null);

        assertEquals("", card.getUrl());
        assertEquals("", card.getTitle());
        assertEquals("", card.getDescription());
        assertEquals("", card.getType());
        assertEquals("", card.getWidth());
        assertEquals("", card.getHeight());
        assertEquals("", card.getImage());
    }

    /**
     * Tests equals and hashCode for equal objects, null, other types and each differing field.
     *
     * @author Owen Van Esbroeck
     */
    @Test
    public void testEqualsAndHashCode() {
        Card base = new Card("url", "title", "description", "link", "100", "200", "image");
        Card same = new Card("url", "title", "description", "link", "100", "200", "image");

        // Reflexivity and type checks
        assertEquals(base, base);
        assertNotEquals(base, null);
        assertNotEquals(base, "not a card");

        // Equality and hashCode contract
        assertEquals(base, same);
        assertEquals(base.hashCode(), same.hashCode());

        // Every field compared by equals
        assertNotEquals(base, new Card("other", "title", "description", "link", "100", "200", "image"));
        assertNotEquals(base, new Card("url", "other", "description", "link", "100", "200", "image"));
        assertNotEquals(base, new Card("url", "title", "other", "link", "100", "200", "image"));
        assertNotEquals(base, new Card("url", "title", "description", "other", "100", "200", "image"));
    }

    /**
     * Tests that toString contains the main card fields.
     *
     * @author Owen Van Esbroeck
     */
    @Test
    public void testToString() {
        Card card = new Card("url", "title", "description", "link", "100", "200", "image");

        String text = card.toString();
        assertTrue(text.contains("url"));
        assertTrue(text.contains("title"));
        assertTrue(text.contains("description"));
        assertTrue(text.contains("link"));
    }
}
