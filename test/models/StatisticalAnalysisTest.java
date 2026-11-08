package models;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Unit tests for the StatisticalAnalysis model, covering getters, sentiment keys,
 * and the JSON serialization helpers used by the frontend charts.
 *
 * @author Owen Van Esbroeck
 */
public class StatisticalAnalysisTest {

    /**
     * Builds a populated analysis used by the serialization tests.
     *
     * @return a StatisticalAnalysis with sentiment, language, visibility and engagement data
     * @author Owen Van Esbroeck
     */
    private static StatisticalAnalysis analysis() {
        Map<String, Long> sentiments = new LinkedHashMap<>();
        sentiments.put(":-)", 2L);
        sentiments.put(":-(", 1L);

        Map<String, Long> languages = new LinkedHashMap<>();
        languages.put("en", 2L);
        languages.put("xx", 1L);

        Map<String, Long> visibility = new LinkedHashMap<>();
        visibility.put("public", 2L);
        visibility.put("private", 1L);

        Map<String, Double> replies = new LinkedHashMap<>();
        replies.put("sum", 3.0);
        replies.put("avg", 1.5);
        replies.put("min", 1.0);
        replies.put("max", 2.0);

        Map<String, Map<String, Double>> engagement = new LinkedHashMap<>();
        engagement.put("Replies", replies);

        return new StatisticalAnalysis(3, sentiments, languages, visibility, engagement);
    }

    /**
     * Tests all getters, the standard sentiment list, and sorted sentiment keys.
     *
     * @author Owen Van Esbroeck
     */
    @Test
    public void testGettersAndSentimentKeys() {
        StatisticalAnalysis analysis = analysis();

        assertEquals(3, analysis.getPostCount());
        assertEquals(Long.valueOf(2), analysis.getSentimentCounts().get(":-)"));
        assertEquals(Long.valueOf(1), analysis.getSentimentCounts().get(":-("));
        assertEquals(Long.valueOf(2), analysis.getLanguageCounts().get("en"));
        assertEquals(Long.valueOf(2), analysis.getVisibilityCounts().get("public"));
        assertEquals(1.5, analysis.getEngagmentCounts().get("Replies").get("avg"), 0.0001);
        assertEquals(List.of(":-)", ":-|", ":-("), analysis.getValidSentiments());
        assertEquals(List.of(":-(", ":-)"), analysis.getSentimentKeys());
    }

    /**
     * Tests that sentiment JSON includes every standard sentiment, defaulting missing counts to zero.
     *
     * @author Owen Van Esbroeck
     */
    @Test
    public void testSentimentCountsAsJson() {
        JsonNode json = analysis().getSentimentCountsAsJSON();

        assertEquals(2, json.get(":-)").asInt());
        assertEquals(1, json.get(":-(").asInt());
        assertEquals(0, json.get(":-|").asInt());
    }

    /**
     * Tests that language JSON uses friendly names and falls back to unknown codes.
     *
     * @author Owen Van Esbroeck
     */
    @Test
    public void testLanguageCountsAsJson() {
        JsonNode json = analysis().getLanguageCountsAsJSON();

        assertEquals(2, json.get("English").asInt());
        assertEquals(1, json.get("xx").asInt());
    }

    /**
     * Tests that visibility JSON contains every visibility level with its count.
     *
     * @author Owen Van Esbroeck
     */
    @Test
    public void testVisibilityCountsAsJson() {
        JsonNode json = analysis().getVisibilityCountsAsJSON();

        assertEquals(2, json.get("public").asInt());
        assertEquals(1, json.get("private").asInt());
    }

    /**
     * Tests engagement JSON serialization and the generic numeric-map converter.
     *
     * @author Owen Van Esbroeck
     */
    @Test
    public void testEngagementCountsAsJsonAndToNode() {
        JsonNode json = analysis().getEngagmentCountsAsJSON();

        assertEquals(3.0, json.get("Replies").get("sum").asDouble(), 0.0001);
        assertEquals(1.5, json.get("Replies").get("avg").asDouble(), 0.0001);
        assertEquals(1.0, json.get("Replies").get("min").asDouble(), 0.0001);
        assertEquals(2.0, json.get("Replies").get("max").asDouble(), 0.0001);

        JsonNode direct = analysis().toNode(Map.of("sum", 3.0));
        assertEquals(3.0, direct.get("sum").asDouble(), 0.0001);
    }
}
