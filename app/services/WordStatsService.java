package services;

import models.Post;
import models.WordStat;

import java.util.List;

/**
 * Service interface for calculating word-level frequency statistics on posts using the Java Streams API.
 *
 * @author Qiang Xue
 */
public interface WordStatsService {

    /**
     * Extracts, tokenizes, counts, and sorts unique words from the given list of posts in descending order of frequency.
     *
     * @param posts list of posts to process
     * @return list of WordStat objects ordered descending by frequency
     * @author Qiang Xue
     */
    List<WordStat> computeWordStats(List<Post> posts);
}
