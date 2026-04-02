package app.lockin.lockin.common.requests;

import app.lockin.lockin.common.models.PostAttachment;

public class CreatePostRequest extends Request {
    private final String textContent;
    private final PostAttachment attachment;

    public CreatePostRequest(String textContent, PostAttachment attachment) {
        this.textContent = textContent;
        this.attachment = attachment;
    }

    public String getTextContent() {
        return textContent;
    }

    public PostAttachment getAttachment() {
        return attachment;
    }

    @Override
    public RequestType getType() {
        return RequestType.CREATE_POST;
    }
}
