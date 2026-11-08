package services;

import models.Account;
import models.Post;
import models.StatisticalAnalysis;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for StatisticalAnalysisServiceImpl, verifying sentiment, language,
 * visibility, and engagement aggregation across posts.
 *
 * @author Owen Van Esbroeck
 */
public class StatisticalAnalysisServiceImplTest {

    /**
     * Tests aggregation of sentiment, language, visibility, and reply/reblog/favourite metrics.
     *
     * @author Owen Van Esbroeck
     */
    @Test
    public void testComputeAnalysisAggregatesAllMetrics() {
        StatisticalAnalysisServiceImpl service = new StatisticalAnalysisServiceImpl();
        Account author = new Account("1", "alice", "Alice", "url", "avatar");
        Post first = new Post("1", author, "time", "en", "hello", "<p>hello</p>", "url1", "public",
                "5", "2", "10", ":-)", null, null);
        Post second = new Post("2", author, "time", "fr", "salut", "<p>salut</p>", "url2", "private",
                "1", "4", "6", ":-(", null, null);

        StatisticalAnalysis analysis = service.computeAnalysis(List.of(first, second));

        assertEquals(2, analysis.getPostCount());
        assertEquals(Long.valueOf(1), analysis.getSentimentCounts().get(":-)"));
        assertEquals(Long.valueOf(1), analysis.getSentimentCounts().get(":-("));
        assertEquals(Long.valueOf(1), analysis.getLanguageCounts().get("en"));
        assertEquals(Long.valueOf(1), analysis.getLanguageCounts().get("fr"));
        assertEquals(Long.valueOf(1), analysis.getVisibilityCounts().get("public"));
        assertEquals(Long.valueOf(1), analysis.getVisibilityCounts().get("private"));

        assertEquals(6.0, analysis.getEngagmentCounts().get("Replies").get("sum"), 0.0001);
        assertEquals(3.0, analysis.getEngagmentCounts().get("Replies").get("avg"), 0.0001);
        assertEquals(1.0, analysis.getEngagmentCounts().get("Replies").get("min"), 0.0001);
        assertEquals(5.0, analysis.getEngagmentCounts().get("Replies").get("max"), 0.0001);

        assertEquals(6.0, analysis.getEngagmentCounts().get("Reblogs").get("sum"), 0.0001);
        assertEquals(3.0, analysis.getEngagmentCounts().get("Reblogs").get("avg"), 0.0001);
        assertEquals(2.0, analysis.getEngagmentCounts().get("Reblogs").get("min"), 0.0001);
        assertEquals(4.0, analysis.getEngagmentCounts().get("Reblogs").get("max"), 0.0001);

        assertEquals(16.0, analysis.getEngagmentCounts().get("Favourites").get("sum"), 0.0001);
        assertEquals(8.0, analysis.getEngagmentCounts().get("Favourites").get("avg"), 0.0001);
        assertEquals(6.0, analysis.getEngagmentCounts().get("Favourites").get("min"), 0.0001);
        assertEquals(10.0, analysis.getEngagmentCounts().get("Favourites").get("max"), 0.0001);
    }
}
