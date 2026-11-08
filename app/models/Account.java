package models;

import java.util.Objects;

/**
 * Represents a Mastodon user account / author of a post.
 * 
 * @author Kush Chaudhari
 * @author Mihir Vipulbhai Panchal
 * @author Owen Van Esbroeck
 * @author Qiang Xue
 */
public class Account {

    /** The unique account identifier. */
    private final String id;
    /** The username handle of the account. */
    private final String username;
    /** The display name of the account holder. */
    private final String displayName;
    /** The URL pointing to the user's profile on Mastodon. */
    private final String url;
    /** The URL pointing to the user's avatar image. */
    private final String avatar;

    /**
     * Constructs a new Account instance.
     *
     * @param id          the unique account identifier
     * @param username    the Mastodon username
     * @param displayName the display name of the user
     * @param url         the URL to the user's profile
     * @param avatar      the URL to the user's avatar image
     * @author Qiang Xue
     */
    public Account(String id, String username, String displayName, String url, String avatar) {
        this.id = id != null ? id : "";
        this.username = username != null ? username : "";
        this.displayName = displayName != null && !displayName.trim().isEmpty() ? displayName : this.username;
        this.url = url != null ? url : "";
        this.avatar = avatar != null ? avatar : "";
    }

    /**
     * Returns the account ID.
     *
     * @return the account ID
     * @author Qiang Xue
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the username.
     *
     * @return the username
     * @author Qiang Xue
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns the user's display name.
     *
     * @return the display name
     * @author Qiang Xue
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the profile URL.
     *
     * @return the profile URL
     * @author Qiang Xue
     */
    public String getUrl() {
        return url;
    }

    /**
     * Returns the avatar image URL.
     *
     * @return the avatar URL
     * @author Qiang Xue
     */
    public String getAvatar() {
        return avatar;
    }

    /**
     * Compares this account to another object for equality.
     *
     * @param o the reference object with which to compare
     * @return {@code true} if this object is equal to the argument; {@code false} otherwise
     * @author Qiang Xue
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return Objects.equals(id, account.id) &&
               Objects.equals(username, account.username) &&
               Objects.equals(displayName, account.displayName) &&
               Objects.equals(url, account.url) &&
               Objects.equals(avatar, account.avatar);
    }

    /**
     * Returns the hash code value for this account.
     *
     * @return a hash code value for this object
     * @author Qiang Xue
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, username, displayName, url, avatar);
    }

    /**
     * Returns a string representation of this account.
     *
     * @return a string representation of the account
     * @author Qiang Xue
     */
    @Override
    public String toString() {
        return "Account{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", displayName='" + displayName + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
