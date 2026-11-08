package services;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for HtmlCleaner utility class using built-in Java standard library.
 *
 * @author Qiang Xue
 */
public class HtmlCleanerTest {

    /**
     * Tests cleaning with null or blank HTML input.
     * @author Qiang Xue
     */
    @Test
    public void testCleanNullOrBlank() {
        assertEquals("", HtmlCleaner.clean(null));
        assertEquals("", HtmlCleaner.clean(""));
        assertEquals("", HtmlCleaner.clean("   "));
    }

    /**
     * Tests stripping simple paragraph and break tags.
     * @author Qiang Xue
     */
    @Test
    public void testCleanParagraphsAndBreaks() {
        String html = "<p>First line.<br>Second line.</p><p>Second paragraph.</p>";
        String cleaned = HtmlCleaner.clean(html);
        assertEquals("First line. Second line. Second paragraph.", cleaned);
    }

    /**
     * Tests stripping links and formatting tags while retaining inner text.
     * @author Qiang Xue
     */
    @Test
    public void testCleanFormattingAndLinks() {
        String html = "<p>Check out <a href=\"https://example.com\">this link</a> and <strong>bold text</strong>.</p>";
        String cleaned = HtmlCleaner.clean(html);
        assertEquals("Check out this link and bold text.", cleaned);
    }

    /**
     * Tests decoding of standard HTML entities as well as numeric and hex entities.
     * @author Qiang Xue
     */
    @Test
    public void testCleanHtmlEntities() {
        String html = "<p>Tom &amp; Jerry &lt;3 cheese &quot;always&quot; &apos;fast&#39;&nbsp;char:&#65;&#x42;</p>";
        String cleaned = HtmlCleaner.clean(html);
        assertEquals("Tom & Jerry <3 cheese \"always\" 'fast' char:AB", cleaned);
    }
}
