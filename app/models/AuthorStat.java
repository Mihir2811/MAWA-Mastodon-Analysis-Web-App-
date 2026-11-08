package models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents an author's activity on one analyzed search-result page.
 *
 * @author Kush Chaudhari
 */
public class AuthorStat {

    /** The author account. */
    private final Account author;
    /** The number of posts published by the author on this result page. */
    private final long postCount;
    /** The latest post published by this author on this result page. */
    private final Post latestPost;
    /** Map of language codes to occurrence counts for this author. */
    private final Map<String, Long> languageCounts;

    /**
     * Constructs a new AuthorStat instance.
     *
     * @param author         the author account
     * @param postCount      the total posts contributed by the author on this page
     * @param latestPost     the newest post by the author on this page
     * @param languageCounts frequency counts of languages used by the author
     * @author Kush Chaudhari
     */
    public AuthorStat(Account author, long postCount, Post latestPost, Map<String, Long> languageCounts) {
        this.author = Objects.requireNonNull(author, "author");
        this.postCount = postCount;
        this.latestPost = Objects.requireNonNull(latestPost, "latestPost");
        this.languageCounts = Collections.unmodifiableMap(new LinkedHashMap<>(
                Objects.requireNonNull(languageCounts, "languageCounts")));
    }

    /**
     * Returns the author account.
     *
     * @return the author account
     * @author Kush Chaudhari
     */
    public Account getAuthor() {
        return author;
    }

    /**
     * Returns the number of posts by this author on the page.
     *
     * @return the post count
     * @author Kush Chaudhari
     */
    public long getPostCount() {
        return postCount;
    }

    /**
     * Returns the latest post by this author.
     *
     * @return the newest Post object
     * @author Kush Chaudhari
     */
    public Post getLatestPost() {
        return latestPost;
    }

    /**
     * Returns the map of language counts.
     *
     * @return unmodifiable map of language codes to counts
     * @author Kush Chaudhari
     */
    public Map<String, Long> getLanguageCounts() {
        return languageCounts;
    }

    /**
     * Returns the list of distinct language codes used by this author.
     *
     * @return unmodifiable list of language code strings
     * @author Kush Chaudhari
     */
    public List<String> getLanguages() {
        return Collections.unmodifiableList(new ArrayList<>(languageCounts.keySet()));
    }
}
