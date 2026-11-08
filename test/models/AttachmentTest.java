package models;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for the Attachment domain model, covering construction, getters,
 * media-type checks, equals/hashCode and toString.
 *
 * @author Owen Van Esbroeck
 */
public class AttachmentTest {

    /**
     * Tests construction with non-null values and all getters.
     *
     * @author Owen Van Esbroeck
     */
    @Test
    public void testConstructorAndGettersWithValues() {
        Attachment attachment = new Attachment("a1", "image", "https://example.com/image.png",
                "https://example.com/preview.png", "An image", "640", "480");

        assertEquals("a1", attachment.getId());
        assertEquals("image", attachment.getType());
        assertEquals("https://example.com/image.png", attachment.getUrl());
        assertEquals("https://example.com/preview.png", attachment.getPreviewUrl());
        assertEquals("An image", attachment.getDescription());
        assertEquals("640", attachment.getWidth());
        assertEquals("480", attachment.getHeight());
    }

    /**
     * Tests the null-safe defaults applied by the constructor for every parameter.
     *
     * @author Owen Van Esbroeck
     */
    @Test
    public void testConstructorWithNullValuesUsesDefaults() {
        Attachment attachment = new Attachment(null, null, null, null, null, null, null);

        assertEquals("", attachment.getId());
        assertEquals("unknown", attachment.getType());
        assertEquals("", attachment.getUrl());
        assertEquals("", attachment.getPreviewUrl());
        assertEquals("", attachment.getDescription());
        assertEquals("0", attachment.getWidth());
        assertEquals("0", attachment.getHeight());
    }

    /**
     * Tests the image and video checks for matching and non-matching attachment types.
     *
     * @author Owen Van Esbroeck
     */
    @Test
    public void testImageAndVideoChecks() {
        Attachment image = new Attachment("1", "image", "url", "preview", "desc", "10", "20");
        Attachment video = new Attachment("2", "video", "url", "preview", "desc", "10", "20");

        assertTrue(image.isImage());
        assertFalse(image.isVideo());
        assertTrue(video.isVideo());
        assertFalse(video.isImage());
    }

    /**
     * Tests videoType for a video with an empty URL and for a non-video attachment.
     *
     * @author Owen Van Esbroeck
     */
    @Test
    public void testVideoType() {
        Attachment video = new Attachment("1", "video", "", "", "", "0", "0");
        Attachment image = new Attachment("2", "image", "https://example.com/image.png", "", "", "0", "0");

        // An empty URL is split into a single empty token, so the method reports the bare prefix.
        assertEquals("video/", video.videoType());
        assertEquals("", image.videoType());
    }

    /**
     * Tests equals and hashCode for equal objects, null, other types and each differing field.
     *
     * @author Owen Van Esbroeck
     */
    @Test
    public void testEqualsAndHashCode() {
        Attachment base = new Attachment("1", "image", "url", "preview", "description", "10", "20");
        Attachment same = new Attachment("1", "image", "url", "preview", "description", "10", "20");

        // Reflexivity and type checks
        assertEquals(base, base);
        assertNotEquals(base, null);
        assertNotEquals(base, "not an attachment");

        // Equality and hashCode contract
        assertEquals(base, same);
        assertEquals(base.hashCode(), same.hashCode());

        // Every field compared by equals
        assertNotEquals(base, new Attachment("2", "image", "url", "preview", "description", "10", "20"));
        assertNotEquals(base, new Attachment("1", "video", "url", "preview", "description", "10", "20"));
        assertNotEquals(base, new Attachment("1", "image", "other", "preview", "description", "10", "20"));
        assertNotEquals(base, new Attachment("1", "image", "url", "preview", "other", "10", "20"));
    }

    /**
     * Tests that toString contains the main attachment fields.
     *
     * @author Owen Van Esbroeck
     */
    @Test
    public void testToString() {
        Attachment attachment = new Attachment("a1", "image", "https://example.com/image.png",
                "preview", "An image", "640", "480");

        String text = attachment.toString();
        assertTrue(text.contains("a1"));
        assertTrue(text.contains("image"));
        assertTrue(text.contains("https://example.com/image.png"));
        assertTrue(text.contains("An image"));
    }
}
