package models;

import java.util.Objects;

/**
 * Represents a preview card of a Mastodon post.
 *
 * @author Owen Van Esbroeck
 */
public class Card {
    /** The target URL of the card. */
    private final String url;
    /** The title of the card. */
    private final String title;
    /** The description of the card. */
    private final String description;
    /** The type of the card (e.g., link, photo, video). */
    private final String type;
    /** The width of the card preview. */
    private final String width;
    /** The height of the card preview. */
    private final String height;
    /** The preview image URL of the card. */
    private final String image;
    //private final String html;


    /**
     * Constructs a new Card instance.
     *
     * @param url         the card url
     * @param title       the card title
     * @param description the card description
     * @param type        the card type
     * @param width       the card width
     * @param height      the card height
     * @param image       the card preview image URL
     * @author Owen Van Esbroeck
     */
    public Card(String url, String title, String description, String type, String width, String height, String image) {
        this.url = url != null ? url : "";
        this.title = title != null ? title : "";
        this.type = type != null ? type : "";
        this.description = description != null ? description : "";
        this.width = width != null ? width : "";
        this.height = height != null ? height : "";
        this.image = image != null ? image : "";
    }

    /**
     * Returns the card URL.
     *
     * @return the card URL
     * @author Owen Van Esbroeck
     */
    public String getUrl() {
        return url;
    }


    /**
     * Returns the card title.
     *
     * @return the card title
     * @author Owen Van Esbroeck
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the card description.
     *
     * @return the card description
     * @author Owen Van Esbroeck
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the card type.
     *
     * @return the card type
     * @author Owen Van Esbroeck
     */
    public String getType() {
        return type;
    }


    /**
     * Returns the card width.
     *
     * @return the card width
     * @author Owen Van Esbroeck
     */
    public String getWidth() { return width; }

    /**
     * Returns the card height.
     *
     * @return the card height
     * @author Owen Van Esbroeck
     */
    public String getHeight() { return height; }

    /**
     * Returns the card preview image URL.
     *
     * @return the preview image URL
     * @author Owen Van Esbroeck
     */
    public String getImage() { return image; }

    /**
     * Compares this card to another object for equality.
     *
     * @param o the reference object with which to compare
     * @return {@code true} if this object is equal to the argument; {@code false} otherwise
     * @author Owen Van Esbroeck
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Card card = (Card) o;
        return Objects.equals(url, card.url) &&
               Objects.equals(title, card.title) &&
               Objects.equals(description, card.description) &&
               Objects.equals(type, card.type);
    }

    /**
     * Returns the hash code value for this card.
     *
     * @return a hash code value for this object
     * @author Owen Van Esbroeck
     */
    @Override
    public int hashCode() {
        return Objects.hash(url, title, description, type);
    }

    /**
     * Returns a string representation of this card.
     *
     * @return a string representation of the card
     * @author Owen Van Esbroeck
     */
    @Override
    public String toString() {
        return "Card{" +
                "url='" + url + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
