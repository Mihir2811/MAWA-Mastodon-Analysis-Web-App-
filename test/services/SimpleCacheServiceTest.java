package services;

import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.*;

/**
 * Unit tests for SimpleCacheService verifying thread-safe in-memory caching and TTL expiration.
 *
 * @author Qiang Xue
 */
public class SimpleCacheServiceTest {

    /** The SimpleCacheService instance under test. */
    private SimpleCacheService cache;

    /**
     * Initializes cache service before each test.
     * @author Qiang Xue
     */
    @Before
    public void setUp() {
        cache = new SimpleCacheService();
    }

    /**
     * Tests basic caching and retrieval.
     * @author Qiang Xue
     */
    @Test
    public void testGetAndSet() {
        cache.set("key1", "value1", 60);
        Optional<String> result = cache.get("key1");
        assertTrue(result.isPresent());
        assertEquals("value1", result.get());
    }

    /**
     * Tests cache miss for non-existent key.
     * @author Qiang Xue
     */
    @Test
    public void testCacheMiss() {
        Optional<String> result = cache.get("nonexistent");
        assertFalse(result.isPresent());
    }

    /**
     * Tests null key handling.
     * @author Qiang Xue
     */
    @Test
    public void testNullKeyHandling() {
        assertFalse(cache.get(null).isPresent());
        cache.set(null, "value", 60);
        cache.set("key", null, 60);
        assertFalse(cache.get("key").isPresent());
    }

    /**
     * Tests cache expiration.
     * @author Qiang Xue
     */
    @Test
    public void testCacheExpiration() throws InterruptedException {
        // Store with 0 seconds TTL so it expires immediately
        cache.set("expiringKey", "tempValue", 0);
        Thread.sleep(10);
        Optional<String> result = cache.get("expiringKey");
        assertFalse(result.isPresent());
    }

    /**
     * Tests cache clearing.
     * @author Qiang Xue
     */
    @Test
    public void testClear() {
        cache.set("k1", "v1", 60);
        cache.set("k2", "v2", 60);
        cache.clear();
        assertFalse(cache.get("k1").isPresent());
        assertFalse(cache.get("k2").isPresent());
    }
}
