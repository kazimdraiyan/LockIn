package app.lockin.lockin.common.models;

import java.io.Serializable;

public class Message implements Serializable {
    private final String id;
    private final String chatId;
    private final String senderUsername;
    private final String text;
    private final MessageAttachment attachment;
    private final long createdAt;
    private final String replyOf;

    public Message(
            String id,
            String chatId,
            String senderUsername,
            String text,
            MessageAttachment attachment,
            long createdAt,
            String replyOf
    ) {
        this.id = id;
        this.chatId = chatId;
        this.senderUsername = senderUsername;
        this.text = text;
        this.attachment = attachment;
        this.createdAt = createdAt;
        this.replyOf = replyOf;
    }

    public String getId() {
        return id;
    }

    public String getChatId() {
        return chatId;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public String getText() {
        return text;
    }

    public MessageAttachment getAttachment() {
        return attachment;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public String getReplyOf() {
        return replyOf;
    }
}
