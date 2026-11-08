package services;

import models.Post;
import models.WordStat;

import javax.inject.Singleton;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Implementation of WordStatsService that uses the Java Streams API to count all unique words
 * across the retrieved posts, ordered by descending frequency (alphabetically on ties).
 *
 * @author Qiang Xue
 */
@Singleton
public class WordStatsServiceImpl implements WordStatsService {

    /** Anything that is not a letter or a digit separates two words. */
    private static final Pattern WORD_DELIMITER_PATTERN = Pattern.compile("[^\\p{L}\\p{Nd}]+");

    /**
     * Extracts, tokenizes, counts, and sorts unique words from the given list of posts in descending order of frequency.
     *
     * @param posts list of posts to process
     * @return list of WordStat objects ordered descending by frequency
     * @author Qiang Xue
     */
    @Override
    public List<WordStat> computeWordStats(List<Post> posts) {
        if (posts == null || posts.isEmpty()) {
            return Collections.emptyList();
        }

        return posts.stream()
                .filter(post -> post != null && post.getContent() != null && !post.getContent().trim().isEmpty())
                .map(Post::getContent)
                .flatMap(content -> Arrays.stream(WORD_DELIMITER_PATTERN.split(content.toLowerCase())))
                .map(String::trim)
                .filter(word -> !word.isEmpty())
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .map(entry -> new WordStat(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }
}
