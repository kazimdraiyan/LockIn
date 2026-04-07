package app.lockin.lockin.common.requests;

import app.lockin.lockin.common.models.Attachment;

public class CreateMessageRequest extends Request {
    private final String chatId;
    private final String recipientUsername; // TODO: Generalize for group chats
    private final String text;
    private final Attachment attachment;
    private final String replyOf;

    public CreateMessageRequest(String chatId, String recipientUsername, String text, Attachment attachment, String replyOf) {
        this.chatId = chatId;
        this.recipientUsername = recipientUsername;
        this.text = text;
        this.attachment = attachment;
        this.replyOf = replyOf;
    }

    public String getChatId() {
        return chatId;
    }

    public String getRecipientUsername() {
        return recipientUsername;
    }

    public String getText() {
        return text;
    }

    public Attachment getAttachment() {
        return attachment;
    }

    public String getReplyOf() {
        return replyOf;
    }

    @Override
    public RequestType getType() {
        return RequestType.CREATE_MESSAGE;
    }
}
