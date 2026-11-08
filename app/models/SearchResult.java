package models;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Encapsulates the results of a Mastodon hashtag query for a single result page.
 * Besides the posts themselves it carries everything the views need to render the
 * {@code [ Previous ] Page N [ Next ]} navigation:
 * <ul>
 *   <li>the pagination parameters ({@code max_id} / {@code min_id}) that produced this page,
 *       so the word statistics page can re-request exactly the same page;</li>
 *   <li>the anchors needed to move to the next page ({@code max_id}, derived from the last post)
 *       and back to the previous page ({@code min_id}, derived from the first post).</li>
 * </ul>
 *
 * @author Qiang Xue
 */
public class SearchResult {

    /** The searched hashtag string. */
    private final String hashtag;
    /** The list of posts retrieved for this result page. */
    private final List<Post> posts;
    /** The 1-based page number. */
    private final int page;
    /** The max_id query parameter used to fetch this page, if any. */
    private final Optional<String> pageMaxId;
    /** The min_id query parameter used to fetch this page, if any. */
    private final Optional<String> pageMinId;
    /** The anchor id to request the next (older) page of results. */
    private final Optional<String> nextMaxId;
    /** The anchor id to request the previous (newer) page of results. */
    private final Optional<String> prevMinId;
    /** The error message if the search failed. */
    private final Optional<String> errorMessage;

    /**
     * Constructs a successful SearchResult for one page of results.
     *
     * @param hashtag   the searched hashtag
     * @param posts     the posts retrieved for this page
     * @param page      the 1-based page number
     * @param maxId     the {@code max_id} parameter used to fetch this page, if any
     * @param minId     the {@code min_id} parameter used to fetch this page, if any
     * @param pageSize  the number of posts requested per page; a full page means more results may exist
     * @author Qiang Xue
     */
    public SearchResult(String hashtag,
                        List<Post> posts,
                        int page,
                        Optional<String> maxId,
                        Optional<String> minId,
                        int pageSize) {
        this.hashtag = hashtag != null ? hashtag : "";
        this.posts = posts != null ? Collections.unmodifiableList(posts) : Collections.emptyList();
        this.page = Math.max(page, 1);
        this.pageMaxId = maxId != null ? maxId : Optional.empty();
        this.pageMinId = minId != null ? minId : Optional.empty();
        this.errorMessage = Optional.empty();

        boolean fullPage = pageSize > 0 && this.posts.size() >= pageSize;
        this.nextMaxId = fullPage
                ? Optional.of(this.posts.get(this.posts.size() - 1).getId())
                : Optional.empty();
        this.prevMinId = this.page > 1 && !this.posts.isEmpty()
                ? Optional.of(this.posts.get(0).getId())
                : Optional.empty();
    }

    /**
     * Constructs an error SearchResult that has no posts and no pagination.
     *
     * @param hashtag      the searched hashtag
     * @param errorMessage the error message to display
     * @author Qiang Xue
     */
    public SearchResult(String hashtag, String errorMessage) {
        this.hashtag = hashtag != null ? hashtag : "";
        this.posts = Collections.emptyList();
        this.page = 1;
        this.pageMaxId = Optional.empty();
        this.pageMinId = Optional.empty();
        this.nextMaxId = Optional.empty();
        this.prevMinId = Optional.empty();
        this.errorMessage = Optional.ofNullable(errorMessage);
    }

    /**
     * Returns the searched hashtag.
     *
     * @return hashtag string
     * @author Qiang Xue
     */
    public String getHashtag() {
        return hashtag;
    }

    /**
     * Returns the list of retrieved posts.
     *
     * @return unmodifiable list of posts
     * @author Qiang Xue
     */
    public List<Post> getPosts() {
        return posts;
    }

    /**
     * Returns the 1-based number of this result page.
     *
     * @return the page number
     * @author Qiang Xue
     */
    public int getPage() {
        return page;
    }

    /**
     * Returns the {@code max_id} parameter that produced this page.
     *
     * @return optional max_id of the current page request
     * @author Qiang Xue
     */
    public Optional<String> getPageMaxId() {
        return pageMaxId;
    }

    /**
     * Returns the {@code min_id} parameter that produced this page.
     *
     * @return optional min_id of the current page request
     * @author Qiang Xue
     */
    public Optional<String> getPageMinId() {
        return pageMinId;
    }

    /**
     * Returns the anchor for the next (older) page, i.e. the id of the last post of this page.
     *
     * @return optional max_id to request the next page
     * @author Qiang Xue
     */
    public Optional<String> getNextMaxId() {
        return nextMaxId;
    }

    /**
     * Returns the anchor for the previous (newer) page, i.e. the id of the first post of this page.
     *
     * @return optional min_id to request the previous page
     * @author Qiang Xue
     */
    public Optional<String> getPrevMinId() {
        return prevMinId;
    }

    /**
     * Checks whether another (older) page can be requested.
     *
     * @return true if a next page link should be offered
     * @author Qiang Xue
     */
    public boolean hasNext() {
        return nextMaxId.isPresent();
    }

    /**
     * Checks whether a previous (newer) page can be requested.
     *
     * @return true if a previous page link should be offered
     * @author Qiang Xue
     */
    public boolean hasPrevious() {
        return prevMinId.isPresent();
    }

    /**
     * Returns the error message, if any.
     *
     * @return optional error message
     * @author Qiang Xue
     */
    public Optional<String> getErrorMessage() {
        return errorMessage;
    }

    /**
     * Checks if the result is an error.
     *
     * @return true if an error occurred, false otherwise
     * @author Qiang Xue
     */
    public boolean hasError() {
        return errorMessage.isPresent();
    }

    /**
     * Checks if the result contains any posts.
     *
     * @return true if posts list is empty, false otherwise
     * @author Qiang Xue
     */
    public boolean isEmpty() {
        return posts.isEmpty();
    }
}
