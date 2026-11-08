package models;

import java.util.Objects;
import java.util.List;

/**
 * Domain model representing a Mastodon post (status).
 * Designed to be transport-agnostic (reusable across REST and future streaming).
 *
 * @author Kush Chaudhari
 * @author Mihir Vipulbhai Panchal
 * @author Owen Van Esbroeck
 * @author Qiang Xue
 */
public class Post {

    /** The unique status identifier. */
    private final String id;
    /** The author account of the post. */
    private final Account author;
    /** The creation timestamp string. */
    private final String createdAt;
    /** The ISO language code of the post content. */
    private final String language;
    /** The cleaned plain text content of the post. */
    private final String content;
    /** The raw HTML content of the post. */
    private final String rawContent;
    /** The URL pointing to the original post on Mastodon. */
    private final String url;
    /** The visibility level (e.g. public, unlisted, private). */
    private final String visibility;
    /** The sentiment symbol assigned to the post (:-), :-(, :-|). */
    private final String sentiment;
    /** The number of replies to this post. */
    private final String repliesCount;
    /** The number of reblogs (boosts) of this post. */
    private final String reblogsCount;
    /** The number of favourites (likes) of this post. */
    private final String favouritesCount;
    /** The list of media attachments associated with this post. */
    private final List<Attachment> attachments;
    /** The preview card associated with link embeds in this post, if any. */
	private final Card card;
    /**
     * Constructs a new Post instance.
     *
     * @param id              the unique status ID
     * @param author          the author account
     * @param createdAt       the creation timestamp
     * @param language        the language code (e.g., "en")
     * @param content         the cleaned, human-readable plain text content
     * @param rawContent      the original HTML content returned by the API
     * @param url             the URL to the original post on Mastodon
     * @param visibility      the visibility level (e.g., "public", "unlisted")
     * @param repliesCount    the number of replies
     * @param reblogsCount    the number of reblogs
     * @param favouritesCount the number of favourites
     * @param sentiment       the sentiment symbol (:-), :-(, :-|)
     * @param attachments     the list of media attachments
     * @param card            the link preview card
     * @author Qiang Xue
     */
    public Post(String id, Account author, String createdAt, String language,
            String content, String rawContent, String url, String visibility, 
            String repliesCount, String reblogsCount, String favouritesCount, 
            String sentiment, List<Attachment> attachments, Card card) {
        this.id = id != null ? id : "";
        this.author = author;
        this.createdAt = createdAt != null ? createdAt : "";
        this.language = language != null ? language : "unknown";
        this.content = content != null ? content : "";
        this.rawContent = rawContent != null ? rawContent : "";
        this.url = url != null ? url : "";
        this.visibility = visibility != null ? visibility : "public";
        this.repliesCount = repliesCount != null ? repliesCount : "0"; //
        this.reblogsCount = reblogsCount != null ? reblogsCount : "0"; 
        this.favouritesCount = favouritesCount != null ? favouritesCount : "0"; 
        this.sentiment = sentiment != null ? sentiment : ":-|";
        this.attachments = attachments != null ? attachments : List.of();
		    this.card = card; 
    }

    /**
     * Alternate constructor for constructing a Post without metrics or attachments.
     *
     * @param id         the unique status ID
     * @param author     the author account
     * @param createdAt  the creation timestamp
     * @param language   the language code (e.g., "en")
     * @param content    the cleaned plain text content
     * @param rawContent the original HTML content
     * @param url        the URL to the post
     * @param visibility the visibility level
     * @author Qiang Xue
     */
    public Post(String id, Account author, String createdAt, String language, String content, String rawContent, String url, String visibility) {
        this(id, author, createdAt, language, content, rawContent, url, visibility, null, null, null, null, null,null);
    }

    /**
     * Returns the status ID.
     *
     * @return the status ID
     * @author Qiang Xue
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the author account.
     *
     * @return the author account
     * @author Qiang Xue
     */
    public Account getAuthor() {
        return author;
    }

    /**
     * Returns the publication date/time string.
     *
     * @return the creation timestamp
     * @author Qiang Xue
     */
    public String getCreatedAt() {
        return createdAt;
    }

    /**
     * Returns the language code of the post.
     *
     * @return the language code
     * @author Qiang Xue
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Returns the cleaned, human-readable plain text content.
     *
     * @return clean post content
     * @author Qiang Xue
     */
    public String getContent() {
        return content;
    }

    /**
     * Returns the raw HTML content.
     *
     * @return raw HTML string
     * @author Qiang Xue
     */
    public String getRawContent() {
        return rawContent;
    }

    /**
     * Returns the link to the original Mastodon post.
     *
     * @return post URL
     * @author Qiang Xue
     */
    public String getUrl() {
        return url;
    }

    /**
     * Returns the visibility setting of the post.
     *
     * @return visibility string
     * @author Qiang Xue
     */
    public String getVisibility() {
        return visibility;
    }

    /**
     * Returns the sentiment symbol for this post.
     *
     * @return {@code :-)}, {@code :-(}, or {@code :-|}
     * @author Mihir Vipulbhai Panchal
     */
    public String getSentiment() {
        return sentiment;
    }


    /**
     * Returns the number of replies for this post.
     *
     * @return the replies count string
     * @author Owen Van Esbroeck
     */
    public String getRepliesCount() {
        return repliesCount;
    }

    /**
     * Returns the number of reblogs for this post.
     *
     * @return the reblogs count string
     * @author Owen Van Esbroeck
     */
    public String getReblogsCount() {
        return reblogsCount;
    }

    /**
     * Returns the number of favourites for this post.
     *
     * @return the favourites count string
     * @author Owen Van Esbroeck
     */
    public String getFavouritesCount() {
        return favouritesCount;
    }

    /**
     * Returns the list of media attachments.
     *
     * @return list of Attachment objects
     * @author Qiang Xue
     */
    public List<Attachment> getAttachments() {
        return attachments;
    }

    /**
     * Checks if this post includes a link preview card.
     *
     * @return {@code true} if a card is present; {@code false} otherwise
     * @author Qiang Xue
     */
	public boolean hasCard() {
    return card != null;
  }

    /**
     * Returns the preview card of the post.
     *
     * @return the Card instance or {@code null} if not present
     * @author Qiang Xue
     */
  public Card getCard() {
		return card;
	}

    /**
     * Compares this post to another object for equality.
     *
     * @param o the reference object with which to compare
     * @return {@code true} if this object is equal to the argument; {@code false} otherwise
     * @author Qiang Xue
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Post post = (Post) o;
        return Objects.equals(id, post.id) &&
               Objects.equals(author, post.author) &&
               Objects.equals(createdAt, post.createdAt) &&
               Objects.equals(language, post.language) &&
               Objects.equals(content, post.content) &&
               Objects.equals(rawContent, post.rawContent) &&
               Objects.equals(url, post.url) &&
               Objects.equals(visibility, post.visibility) &&
               Objects.equals(repliesCount, post.repliesCount) &&
               Objects.equals(reblogsCount, post.reblogsCount) &&
               Objects.equals(favouritesCount, post.favouritesCount) &&
               Objects.equals(sentiment, post.sentiment);
    }


    /**
     * Returns the hash code value for this post.
     *
     * @return a hash code value for this object
     * @author Qiang Xue
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, author, createdAt, language, content, rawContent, url, visibility, sentiment);
    }

    /**
     * Returns a string representation of this post.
     *
     * @return a string representation of the post
     * @author Qiang Xue
     */
    @Override
    public String toString() {
        return "Post{" +
                "id='" + id + '\'' +
                ", author=" + (author != null ? author.getUsername() : "null") +
                ", createdAt='" + createdAt + '\'' +
                ", language='" + language + '\'' +
                ", visibility='" + visibility + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
