package services;

import models.Post;
import models.StatisticalAnalysis;
import models.WordStat;

import javax.inject.Singleton;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Implementation of StatisticalAnalysisService that aggregates sentiment, language,
 * visibility, and engagement metrics (replies, reblogs, favourites) across retrieved posts.
 *
 * @author Owen Van Esbroeck
 */
@Singleton
public class StatisticalAnalysisServiceImpl implements StatisticalAnalysisService {

    /**
     * Computes statistical aggregates across the provided list of posts using Java Streams.
     *
     * @param posts the list of posts to analyze
     * @return a StatisticalAnalysis object containing the computed metrics
     * @author Owen Van Esbroeck
     */
    @Override
    public StatisticalAnalysis computeAnalysis(List<Post> posts) {
        Map<String, Long> sentiments = posts.stream().collect(Collectors.groupingBy(
            item -> item.getSentiment(),
            Collectors.counting()
        ));
        Map<String, Long> languages = posts.stream().collect(Collectors.groupingBy(
            item -> item.getLanguage(),
            Collectors.counting()
        ));        
        Map<String, Long> visibility = posts.stream().collect(Collectors.groupingBy(
            item -> item.getVisibility(),
            Collectors.counting()
        ));
        Map<String, List<Double>> counts = Map.of(
            "Replies", posts.stream().map(Post::getRepliesCount).map(Double::valueOf).toList(),
            "Reblogs", posts.stream().map(Post::getReblogsCount).map(Double::valueOf).toList(),
            "Favourites", posts.stream().map(Post::getFavouritesCount).map(Double::valueOf).toList()
        );
        Map<String, Map<String, Double>> engagement = new HashMap<>();
        counts.keySet().stream().forEach(key -> 
            {
                List<Double> values = counts.get(key);
                engagement.put(key, Map.of(
                    "sum",
                    values.stream().reduce(0.0,(a,b)-> a+b),
                    "avg", 
                    values.stream().reduce(0.0,(a,b)-> a+b)/values.size(),
                    "min",
                    values.stream().reduce(values.get(0),(a,b) -> Math.min(a,b)),
                    "max",
                    values.stream().reduce(values.get(0),(a,b) -> Math.max(a,b))
                ));
            }
        );
        System.out.println(engagement);
        
        return new StatisticalAnalysis(posts.size(), sentiments, languages, visibility, engagement);
    }
}
