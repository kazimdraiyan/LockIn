package app.lockin.lockin.common.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Post implements Serializable {
    private final String id;
    private final String authorUsername;
    private final String textContent;
    private final Attachment attachment;
    private final long createdAt;
    private final ArrayList<Comment> comments;

    public Post(String id, String authorUsername, String textContent, Attachment attachment, long createdAt, List<Comment> comments) {
        this.id = id;
        this.authorUsername = authorUsername;
        this.textContent = textContent;
        this.attachment = attachment;
        this.createdAt = createdAt;
        this.comments = comments == null ? new ArrayList<>() : new ArrayList<>(comments);
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

    public Attachment getAttachment() {
        return attachment;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public ArrayList<Comment> getComments() {
        return new ArrayList<>(comments);
    }
}
