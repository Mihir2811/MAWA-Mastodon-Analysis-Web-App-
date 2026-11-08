package services;

import models.Account;
import models.AuthorStat;
import models.Post;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Counts each author's contributions and activity details on one fetched search-result page.
 *
 * @author Kush Chaudhari
 */
@Singleton
public class AuthorAnalysisServiceImpl implements AuthorAnalysisService {

    /**
     * Counts posts and languages per author, preserving each author's newest post, then orders
     * authors from most to fewest contributions.
     *
     * @param posts posts from one fetched hashtag search-result page, newest first
     * @return author contribution statistics in descending post-count order
     * @author Kush Chaudhari
     */
    @Override
    public List<AuthorStat> computeAuthorStats(List<Post> posts) {
        if (posts == null || posts.isEmpty()) {
            return List.of();
        }

        Map<String, AuthorSummary> summaries = new LinkedHashMap<>();
        for (Post post : posts) {
            if (post == null || post.getAuthor() == null) {
                continue;
            }

            Account author = post.getAuthor();
            String key = authorKey(author);
            if (!key.isEmpty()) {
                AuthorSummary summary = summaries.computeIfAbsent(key, ignored -> new AuthorSummary(author, post));
                summary.postCount++;
                summary.languageCounts.merge(post.getLanguage(), 1L, Long::sum);
            }
        }

        List<AuthorStat> stats = new ArrayList<>();
        for (AuthorSummary summary : summaries.values()) {
            stats.add(new AuthorStat(summary.author, summary.postCount, summary.latestPost, summary.languageCounts));
        }

        return stats.stream()
                .sorted(Comparator.comparingLong(AuthorStat::getPostCount)
                        .reversed()
                        .thenComparing(stat -> stat.getAuthor().getDisplayName(), String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(stat -> stat.getAuthor().getUsername(), String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toUnmodifiableList());
    }

    /**
     * Generates a unique deduplication key for an author based on ID, username, or profile URL.
     *
     * @param author the author account
     * @return a distinct string key identifying the author
     * @author Kush Chaudhari
     */
    private static String authorKey(Account author) {
        if (!author.getId().isEmpty()) {
            return "id:" + author.getId();
        }
        if (!author.getUsername().isEmpty()) {
            return "username:" + author.getUsername().toLowerCase(Locale.ROOT);
        }
        if (!author.getUrl().isEmpty()) {
            return "url:" + author.getUrl();
        }
        return "";
    }

    /**
     * Internal mutable accumulator holding intermediate author statistics during aggregation.
     *
     * @author Kush Chaudhari
     */
    private static final class AuthorSummary {
        /** The author account. */
        private final Account author;
        /** The newest post recorded for this author. */
        private final Post latestPost;
        /** Frequency of posts per language for this author. */
        private final Map<String, Long> languageCounts = new LinkedHashMap<>();
        /** Total count of posts recorded for this author. */
        private long postCount;

        /**
         * Constructs an initial AuthorSummary for an author.
         *
         * @param author     the author account
         * @param latestPost the first (newest) post encountered for this author
         * @author Kush Chaudhari
         */
        private AuthorSummary(Account author, Post latestPost) {
            this.author = author;
            this.latestPost = latestPost;
        }
    }
}
