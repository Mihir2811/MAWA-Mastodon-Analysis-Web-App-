package services;

import javax.inject.Singleton;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe, non-blocking in-memory cache implemented with pure Java SE ConcurrentHashMap.
 * Eliminates the need for external caching libraries.
 *
 * @author Qiang Xue
 */
@Singleton
public class SimpleCacheService {

    /**
     * Internal container storing a cached item together with its absolute expiration timestamp.
     *
     * @param <T> the type of cached item
     * @author Qiang Xue
     */
    private static class CacheEntry<T> {
        /** The cached item value. */
        final T value;
        /** The timestamp in milliseconds when this entry expires. */
        final long expiresAtMillis;

        /**
         * Constructs a new CacheEntry with an expiration time computed from the TTL.
         *
         * @param value      the cached item value
         * @param ttlSeconds the time-to-live in seconds
         * @author Qiang Xue
         */
        CacheEntry(T value, long ttlSeconds) {
            this.value = value;
            this.expiresAtMillis = System.currentTimeMillis() + (ttlSeconds * 1000L);
        }

        /**
         * Checks whether this cache entry has expired past its timestamp.
         *
         * @return {@code true} if expired; {@code false} otherwise
         * @author Qiang Xue
         */
        boolean isExpired() {
            return System.currentTimeMillis() > expiresAtMillis;
        }
    }

    /** In-memory concurrent map storing cache entries by string key. */
    private final ConcurrentHashMap<String, CacheEntry<Object>> cache = new ConcurrentHashMap<>();

    /**
     * Retrieves an entry from cache if present and not expired.
     *
     * @param <T> the type of cached item
     * @param key the cache key
     * @return Optional containing the cached value, or empty if absent/expired
     * @author Qiang Xue
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(String key) {
        if (key == null) {
            return Optional.empty();
        }
        CacheEntry<Object> entry = cache.get(key);
        if (entry == null) {
            return Optional.empty();
        }
        if (entry.isExpired()) {
            cache.remove(key, entry);
            return Optional.empty();
        }
        return Optional.of((T) entry.value);
    }

    /**
     * Stores an item in the cache with a time-to-live.
     *
     * @param <T>        the type of cached item
     * @param key        the cache key
     * @param value      the value to store
     * @param ttlSeconds time-to-live in seconds
     * @author Qiang Xue
     */
    public <T> void set(String key, T value, long ttlSeconds) {
        if (key != null && value != null) {
            cache.put(key, new CacheEntry<>(value, ttlSeconds));
        }
    }

    /**
     * Clears all cached entries.
     * @author Qiang Xue
     */
    public void clear() {
        cache.clear();
    }
}
