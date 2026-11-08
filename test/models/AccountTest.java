package models;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for Account domain model, testing all constructors, getters, equals, hashCode, and toString methods.
 *
 * @author Kush Chaudhari
 * @author Mihir Vipulbhai Panchal
 * @author Owen Van Esbroeck
 * @author Qiang Xue
 */
public class AccountTest {

    /**
     * Tests account instantiation with non-null values.
     * @author Qiang Xue
     */
    @Test
    public void testAccountCreationWithStandardValues() {
        Account account = new Account("101", "alice", "Alice Wonderland", "https://mastodon.social/@alice", "https://mastodon.social/avatar.png");
        assertEquals("101", account.getId());
        assertEquals("alice", account.getUsername());
        assertEquals("Alice Wonderland", account.getDisplayName());
        assertEquals("https://mastodon.social/@alice", account.getUrl());
        assertEquals("https://mastodon.social/avatar.png", account.getAvatar());
    }

    /**
     * Tests account instantiation with null values to verify safe fallback defaults.
     * @author Qiang Xue
     */
    @Test
    public void testAccountCreationWithNullValues() {
        Account account = new Account(null, null, null, null, null);
        assertEquals("", account.getId());
        assertEquals("", account.getUsername());
        assertEquals("", account.getDisplayName());
        assertEquals("", account.getUrl());
        assertEquals("", account.getAvatar());
    }

    /**
     * Tests fallback to username when displayName is blank or whitespace.
     * @author Qiang Xue
     */
    @Test
    public void testAccountDisplayNameFallbackToUsername() {
        Account account = new Account("102", "bob", "   ", "https://example.com/bob", "");
        assertEquals("bob", account.getDisplayName());
    }

    /**
     * Tests equals and hashCode contract across different equivalence partitions.
     * @author Qiang Xue
     */
    @Test
    public void testAccountEqualsAndHashCode() {
        Account acc1 = new Account("1", "user", "User Name", "url1", "avatar1");
        Account acc2 = new Account("1", "user", "User Name", "url1", "avatar1");
        Account accDifferentId = new Account("2", "user", "User Name", "url1", "avatar1");
        Account accDifferentUser = new Account("1", "other", "User Name", "url1", "avatar1");
        Account accDifferentDisplay = new Account("1", "user", "Other Name", "url1", "avatar1");
        Account accDifferentUrl = new Account("1", "user", "User Name", "url2", "avatar1");
        Account accDifferentAvatar = new Account("1", "user", "User Name", "url1", "avatar2");

        // Reflexivity & null / type check
        assertEquals(acc1, acc1);
        assertNotEquals(acc1, null);
        assertNotEquals(acc1, "different type");

        // Equality
        assertEquals(acc1, acc2);
        assertEquals(acc1.hashCode(), acc2.hashCode());

        // Inequality
        assertNotEquals(acc1, accDifferentId);
        assertNotEquals(acc1, accDifferentUser);
        assertNotEquals(acc1, accDifferentDisplay);
        assertNotEquals(acc1, accDifferentUrl);
        assertNotEquals(acc1, accDifferentAvatar);
    }

    /**
     * Tests toString output contains expected identifiers.
     * @author Qiang Xue
     */
    @Test
    public void testAccountToString() {
        Account account = new Account("101", "alice", "Alice", "https://mastodon.social/@alice", "avatar.png");
        String str = account.toString();
        assertTrue(str.contains("101"));
        assertTrue(str.contains("alice"));
        assertTrue(str.contains("Alice"));
    }
}
