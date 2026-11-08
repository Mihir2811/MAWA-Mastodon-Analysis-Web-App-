package models;

import java.util.Objects;

/**
 * Represents an attachment of a Mastodon post.
 *
 * @author Owen Van Esbroeck
 */
public class Attachment {

    /** The unique attachment identifier. */
    private final String id;
    /** The attachment type (e.g., image, video, gifv). */
    private final String type;
    /** The URL to the attachment media. */
    private final String url;
    //private final String remote_url;
    /** The URL to the preview thumbnail of the attachment. */
    private final String preview_url;
    //private final String text_url;
    //private final Meta meta;
    /** An alternative text or description for the attachment. */
    private String description;
    /** The display width of the attachment. */
    private final String width;
    /** The display height of the attachment. */
    private final String height;

    /**
     * Constructs a new Attachment instance.
     *
     * @param id          the unique attachment identifier
     * @param type        the attachment type
     * @param url         the URL to the attachment
     * @param preview_url the preview thumbnail URL of the attachment
     * @param description the description of the attachment
     * @param width       the width of the attachment
     * @param height      the height of the attachment
     *
     * @author Owen Van Esbroeck
     */
    public Attachment(String id, String type, String url, String preview_url, String description, String width, String height) {
        this.id = id != null ? id : "";
        this.type = type != null ? type : "unknown";
        this.url = url != null ? url : "";
        this.preview_url = preview_url != null ? preview_url : "";
        this.description = description != null ? description : "";
        this.width = width != null ? width : "0";
        this.height = height != null ? height : "0";
  }

    /**
     * Returns the attachment ID.
     *
     * @return the attachment ID
     * @author Owen Van Esbroeck
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the attachment type.
     *
     * @return the attachment type
     * @author Owen Van Esbroeck
     */
    public String getType() {
        return type;
    }

    /**
     * Returns the attachment url.
     *
     * @return the attachment url
     * @author Owen Van Esbroeck
     */
    public String getUrl() {
        return url;
    }

    /**
     * Returns the preview thumbnail URL.
     *
     * @return the preview thumbnail URL
     * @author Owen Van Esbroeck
     */
    public String getPreviewUrl() {
        return preview_url;
    }

    /**
     * Returns the attachment description.
     *
     * @return the attachment description
     * @author Owen Van Esbroeck
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the width of the attachment.
     *
     * @return the attachment width
     * @author Owen Van Esbroeck
     */
    public String getWidth() {
        return width;
    }

    /**
     * Returns the height of the attachment.
     *
     * @return the attachment height
     * @author Owen Van Esbroeck
     */
    public String getHeight() {
        return height;
    }

    /**
     * Checks if this attachment is an image.
     *
     * @return {@code true} if the attachment type is "image"; {@code false} otherwise
     * @author Owen Van Esbroeck
     */
	public boolean isImage() {
		return this.type.equals("image");
	}

    /**
     * Checks if this attachment is a video.
     *
     * @return {@code true} if the attachment type is "video"; {@code false} otherwise
     * @author Owen Van Esbroeck
     */
	public boolean isVideo() {
		return this.type.equals("video");
	}

    /**
     * Determines the video MIME subtype based on file extension.
     *
     * @return the video content type string, or empty string if not a video
     * @author Owen Van Esbroeck
     */
	public String videoType() {
		if(this.type != "video") { return ""; }
		String[] parts = this.url.split(".");
		return "video/" + parts[parts.length -1];
	}

    /**
     * Compares this attachment to another object for equality.
     *
     * @param o the reference object with which to compare
     * @return {@code true} if this object is equal to the argument; {@code false} otherwise
     * @author Owen Van Esbroeck
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Attachment attachment = (Attachment) o;
        return Objects.equals(id, attachment.id) &&
               Objects.equals(type, attachment.type) &&
               Objects.equals(url, attachment.url) &&
               Objects.equals(description, attachment.description);
    }

    /**
     * Returns the hash code value for this attachment.
     *
     * @return a hash code value for this object
     * @author Owen Van Esbroeck
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, type, url, description);
    }

    /**
     * Returns a string representation of this attachment.
     *
     * @return a string representation of the attachment
     * @author Owen Van Esbroeck
     */
    @Override
    public String toString() {
        return "Attachment{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", url='" + url + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
