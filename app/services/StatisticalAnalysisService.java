package services;

import models.Post;
import models.StatisticalAnalysis;

import java.util.List;

/**
 * Service interface for computing statistical analysis over retrieved Mastodon posts.
 *
 * @author Owen Van Esbroeck
 */
public interface StatisticalAnalysisService {

    /**
     * Computes statistical aggregates (sentiment, language, visibility, and engagement metrics)
     * across the provided list of posts.
     *
     * @param posts the list of posts to analyze
     * @return a StatisticalAnalysis object containing the computed metrics
     * @author Owen Van Esbroeck
     */
    StatisticalAnalysis computeAnalysis(List<Post> posts);
}
