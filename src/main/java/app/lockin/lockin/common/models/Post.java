package app.lockin.lockin.common.models;

import java.io.Serializable;

public class Post implements Serializable {
    private final String id;
    private final String authorUsername;
    private final String textContent;
    private final PostAttachment attachment;
    private final long createdAt;

    public Post(String id, String authorUsername, String textContent, PostAttachment attachment, long createdAt) {
        this.id = id;
        this.authorUsername = authorUsername;
        this.textContent = textContent;
        this.attachment = attachment;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getAuthorUsername() {
        return authorUsername;
    }

    public String getTextContent() {
        return textContent;
    }

    public PostAttachment getAttachment() {
        return attachment;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}
