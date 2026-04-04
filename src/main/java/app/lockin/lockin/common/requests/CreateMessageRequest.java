package app.lockin.lockin.common.requests;

import app.lockin.lockin.common.models.MessageAttachment;

public class CreateMessageRequest extends Request {
    private final String recipientUsername; // TODO: Generalize for group chats
    private final String text;
    private final MessageAttachment attachment;
    private final String replyOf;

    public CreateMessageRequest(String recipientUsername, String text, MessageAttachment attachment, String replyOf) {
        this.recipientUsername = recipientUsername;
        this.text = text;
        this.attachment = attachment;
        this.replyOf = replyOf;
    }

    public String getRecipientUsername() {
        return recipientUsername;
    }

    public String getText() {
        return text;
    }

    public MessageAttachment getAttachment() {
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
