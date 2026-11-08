package services;

import models.SearchResult;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

/**
 * Service interface for searching Mastodon statuses, managing caching, and converting API responses.
 *
 * @author Qiang Xue
 */
public interface MastodonService {

    /**
     * Retrieves one page of posts matching a hashtag, utilizing a cache to avoid duplicate remote requests.
     *
     * @param hashtag the hashtag to search (without '#')
     * @param maxId   optional max_id, used to request the next (older) page
     * @param minId   optional min_id, used to request the previous (newer) page
     * @param page    the 1-based page number being requested
     * @return a CompletionStage holding the SearchResult for that page
     * @author Qiang Xue
     */
    CompletionStage<SearchResult> searchHashtag(String hashtag, Optional<String> maxId, Optional<String> minId, int page);
}
