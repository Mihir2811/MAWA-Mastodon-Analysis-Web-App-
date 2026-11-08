package services;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

/**
 * Interface defining outbound HTTP operations for communicating with the Mastodon REST API.
 * Abstracted to allow easy mocking in automated tests without triggering live network calls.
 *
 * @author Qiang Xue
 */
public interface MastodonClient {

    /** Maximum number of statuses the Mastodon API returns in a single timeline request. */
    int MAX_LIMIT = 40;

    /**
     * Retrieves public posts for a given hashtag from the Mastodon REST API asynchronously.
     *
     * @param hashtag the hashtag to query (without the '#' symbol)
     * @param limit   maximum number of posts to retrieve, capped at {@code MAX_LIMIT} (40)
     * @param maxId   optional max_id token, requesting results older than this status id
     * @param minId   optional min_id token, requesting results newer than this status id
     * @return a CompletionStage containing the parsed JsonNode array of statuses
     * @author Qiang Xue
     */
    CompletionStage<JsonNode> getTagTimeline(String hashtag, int limit, Optional<String> maxId, Optional<String> minId);
}
