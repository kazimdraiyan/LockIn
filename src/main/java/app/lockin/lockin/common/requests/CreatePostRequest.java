package app.lockin.lockin.common.requests;

import app.lockin.lockin.common.models.Attachment;

public class CreatePostRequest extends Request {
    private final String textContent;
    private final Attachment attachment;

    public CreatePostRequest(String textContent, Attachment attachment) {
        this.textContent = textContent;
        this.attachment = attachment;
    }

    public String getTextContent() {
        return textContent;
    }

    public Attachment getAttachment() {
        return attachment;
    }

    @Override
    public RequestType getType() {
        return RequestType.CREATE_POST;
    }
}
