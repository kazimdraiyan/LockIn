package app.lockin.lockin.common.models;

import java.io.Serializable;

public class Comment implements Serializable {
    private final String id;
    private final String postId;
    private final String authorUsername;
    private final String textContent;
    private final Attachment attachment;
    private final long createdAt;

    public Comment(String id, String postId, String authorUsername, String textContent, Attachment attachment, long createdAt) {
        this.id = id;
        this.postId = postId;
        this.authorUsername = authorUsername;
        this.textContent = textContent;
        this.attachment = attachment;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getPostId() {
        return postId;
    }

    public String getAuthorUsername() {
        return authorUsername;
    }

    public String getTextContent() {
        return textContent;
    }

    public Attachment getAttachment() {
        return attachment;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}
