package services;

import models.AuthorStat;
import models.Post;

import java.util.List;

/**
 * Computes author contribution statistics for a list of posts.
 *
 * @author Kush Chaudhari
 */
public interface AuthorAnalysisService {

    /**
     * Counts posts and languages per author, preserving each author's newest post, then orders
     * authors from most to fewest contributions.
     *
     * @param posts posts from one fetched hashtag search-result page, newest first
     * @return author contribution statistics in descending post-count order
     * @author Kush Chaudhari
     */
    List<AuthorStat> computeAuthorStats(List<Post> posts);
}
