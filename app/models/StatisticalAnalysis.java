package models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import play.libs.Json;

/**
 * Encapsulates the statistical analysis computed over a page of retrieved Mastodon posts.
 * Includes breakdowns by sentiment, language, visibility, and engagement metrics (replies,
 * reblogs, favourites) along with helper methods to format data as JSON for frontend charts.
 *
 * @author Owen Van Esbroeck
 */
public class StatisticalAnalysis {
    /** Jackson object mapper used for JSON serialization. */
    private final ObjectMapper mapper = new ObjectMapper();
    /** Total number of posts analyzed. */
    private final long postCount;
    /** Frequency counts keyed by sentiment symbol. */
    private final Map<String, Long> sentimentCounts;
    /** Frequency counts keyed by language code. */
    private final Map<String, Long> languageCounts;
    /** Frequency counts keyed by post visibility level. */
    private final Map<String, Long> visibilityCounts;
    /** Engagement metrics (sum, avg, min, max) for replies, reblogs, and favourites. */
    private final Map<String, Map<String, Double>> engagementCounts;
    /** List of standard sentiment symbols recognized by the application. */
    private final List<String> validSentiments = new ArrayList<>(List.of(":-)", ":-|", ":-("));
    /** Human-readable names mapped from common ISO language codes. */
    private final Map<String, String> languageNames = new HashMap<>(Map.of("en", "English", "fr", "French", "unknown", "None", "de", "German", "es", "Spanish"));

    /**
     * Constructs a new StatisticalAnalysis summary.
     *
     * @param postCount        total number of posts analyzed
     * @param sentimentCounts  counts grouped by sentiment symbol
     * @param languageCounts   counts grouped by language code
     * @param visibilityCounts counts grouped by visibility setting
     * @param engagementCounts engagement metrics (sum, avg, min, max) per interaction type
     * @author Owen Van Esbroeck
     */
    public StatisticalAnalysis(long postCount, Map<String, Long> sentimentCounts, 
        Map<String, Long> languageCounts, Map<String, Long> visibilityCounts, Map<String, Map<String, Double>> engagementCounts) {
        this.postCount = postCount;
        this.languageCounts = Collections.unmodifiableMap(new LinkedHashMap<>(languageCounts));
        this.sentimentCounts = Collections.unmodifiableMap(new LinkedHashMap<>(sentimentCounts));
        this.visibilityCounts = Collections.unmodifiableMap(new LinkedHashMap<>(visibilityCounts)); 
        this.engagementCounts = Collections.unmodifiableMap(new LinkedHashMap<>(engagementCounts)); 

    }

    /**
     * Returns the total number of posts analyzed.
     *
     * @return the post count
     * @author Owen Van Esbroeck
     */
    public long getPostCount() {
        return postCount;
    }

    /**
     * Returns the counts grouped by sentiment symbol.
     *
     * @return map of sentiment symbols to occurrence counts
     * @author Owen Van Esbroeck
     */
    public Map<String, Long> getSentimentCounts() {
        return sentimentCounts;
    }

    /**
     * Returns sorted list of sentiment keys present in the counts.
     *
     * @return sorted list of sentiment strings
     * @author Owen Van Esbroeck
     */
    public List<String> getSentimentKeys() {
        return sentimentCounts.keySet().stream().sorted().collect(Collectors.toList());
    }

    /**
     * Returns the counts grouped by language code.
     *
     * @return map of language codes to occurrence counts
     * @author Owen Van Esbroeck
     */
    public Map<String, Long> getLanguageCounts() {
        return languageCounts;
    }

    /**
     * Returns the counts grouped by visibility.
     *
     * @return map of visibility levels to occurrence counts
     * @author Owen Van Esbroeck
     */
    public Map<String, Long> getVisibilityCounts() {
        return visibilityCounts;
    }

    /**
     * Returns the engagement statistics per metric.
     *
     * @return map of metric names to sub-maps of stat names and values
     * @author Owen Van Esbroeck
     */
    public Map<String, Map<String, Double>> getEngagmentCounts() {
        return engagementCounts;
    }

    /**
     * Returns the list of standard valid sentiments.
     *
     * @return list of sentiment symbol strings
     * @author Owen Van Esbroeck
     */
    public List<String> getValidSentiments() {
        return validSentiments;
    }

    /**
     * Serializes sentiment counts as a JSON object node for frontend charts.
     *
     * @return JsonNode containing sentiment counts
     * @author Owen Van Esbroeck
     */
    public JsonNode getSentimentCountsAsJSON() {
        ObjectNode node = mapper.createObjectNode();
        for(String sentiment : validSentiments) {
            node.put(sentiment, sentimentCounts.getOrDefault(sentiment,Long.valueOf(0)));
        }
        return node;
    }

    /**
     * Serializes language counts as a JSON object node with human-readable language names.
     *
     * @return JsonNode containing language counts
     * @author Owen Van Esbroeck
     */
    public JsonNode getLanguageCountsAsJSON() {
        ObjectNode node = mapper.createObjectNode();
        for(String languageKey :  languageCounts.keySet()) {
            node.put(
                languageNames.getOrDefault(languageKey, languageKey), 
                languageCounts.get(languageKey)
            );
        }
        return node;
    }

    /**
     * Serializes visibility counts as a JSON object node.
     *
     * @return JsonNode containing visibility counts
     * @author Owen Van Esbroeck
     */
    public JsonNode getVisibilityCountsAsJSON() {
        ObjectNode node = mapper.createObjectNode();
        for(String visibilityKey :  visibilityCounts.keySet()) {
            node.put(
                visibilityKey,
                visibilityCounts.get(visibilityKey)
            );
        }
        return node;
    }

    /**
     * Serializes engagement metrics as a nested JSON object node.
     *
     * @return JsonNode containing engagement metrics
     * @author Owen Van Esbroeck
     */
    public JsonNode getEngagmentCountsAsJSON() {
        ObjectNode node = mapper.createObjectNode();
        System.out.println(engagementCounts);
        for(String metric :  engagementCounts.keySet()) {
            node.put(
                metric,
                toNode(engagementCounts.get(metric))
            );
        }
        return node;
    }

    /**
     * Converts a map of numeric statistics into a JSON ObjectNode.
     *
     * @param input map of statistic names to numeric values
     * @return JsonNode representing the map
     * @author Owen Van Esbroeck
     */
    public JsonNode toNode(Map<String, Double> input) {
        ObjectNode node = mapper.createObjectNode();
        input.forEach((key,val) -> node.put(key, val)); 
        return node;
    }
}


