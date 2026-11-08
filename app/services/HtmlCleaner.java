package services;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility for converting Mastodon HTML post content into clean, human-readable plain text
 * using pure built-in Java standard library (regex and string manipulation).
 *
 * @author Qiang Xue
 */
public final class HtmlCleaner {

    /** Pattern matching HTML line break tags (e.g. &lt;br&gt;, &lt;br/&gt;). */
    private static final Pattern BR_PATTERN = Pattern.compile("(?i)<br\\s*/?>");
    /** Pattern matching HTML paragraph end tags (&lt;/p&gt;). */
    private static final Pattern P_END_PATTERN = Pattern.compile("(?i)</p>");
    /** Pattern matching any generic HTML tag. */
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]*>");
    /** Pattern matching decimal or hexadecimal numeric HTML entities. */
    private static final Pattern NUMERIC_ENTITY_PATTERN = Pattern.compile("&#(\\d+);|&#x([0-9a-fA-F]+);");

    /** Pre-defined mapping of common named HTML entities to plain-text equivalents. */
    private static final Map<String, String> COMMON_ENTITIES = new HashMap<>();
    static {
        COMMON_ENTITIES.put("&amp;", "&");
        COMMON_ENTITIES.put("&lt;", "<");
        COMMON_ENTITIES.put("&gt;", ">");
        COMMON_ENTITIES.put("&quot;", "\"");
        COMMON_ENTITIES.put("&#39;", "'");
        COMMON_ENTITIES.put("&apos;", "'");
        COMMON_ENTITIES.put("&nbsp;", " ");
    }

    /**
     * Private constructor to prevent instantiation.
     * @author Qiang Xue
     */
    private HtmlCleaner() {
    }

    /**
     * Cleans an HTML string into readable plain text using standard Java.
     * Preserves paragraph separation and line breaks, removes tags, and decodes HTML entities.
     *
     * @param html raw HTML string from Mastodon API
     * @return clean readable plain text
     * @author Qiang Xue
     */
    public static String clean(String html) {
        if (html == null || html.trim().isEmpty()) {
            return "";
        }

        // Replace breaks and paragraph closings with spaces/newlines
        String text = BR_PATTERN.matcher(html).replaceAll(" ");
        text = P_END_PATTERN.matcher(text).replaceAll(" ");

        // Strip remaining HTML tags
        text = HTML_TAG_PATTERN.matcher(text).replaceAll("");

        // Decode named HTML entities
        for (Map.Entry<String, String> entry : COMMON_ENTITIES.entrySet()) {
            text = text.replace(entry.getKey(), entry.getValue());
        }

        // Decode numeric and hex character entities (e.g. &#65; or &#x41;)
        Matcher matcher = NUMERIC_ENTITY_PATTERN.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            int codePoint;
            if (matcher.group(1) != null) {
                codePoint = Integer.parseInt(matcher.group(1));
            } else {
                codePoint = Integer.parseInt(matcher.group(2), 16);
            }
            matcher.appendReplacement(sb, Matcher.quoteReplacement(new String(Character.toChars(codePoint))));
        }
        matcher.appendTail(sb);

        // Normalize multiple spaces and trim
        return sb.toString().replaceAll("[ \\t]+", " ").trim();
    }
}
