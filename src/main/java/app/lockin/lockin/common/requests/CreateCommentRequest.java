package app.lockin.lockin.common.requests;

import app.lockin.lockin.common.models.Attachment;

public class CreateCommentRequest extends Request {
    private final String postId;
    private final String textContent;
    private final Attachment attachment;

    public CreateCommentRequest(String postId, String textContent, Attachment attachment) {
        this.postId = postId;
        this.textContent = textContent;
        this.attachment = attachment;
    }

    public String getPostId() {
        return postId;
    }

    public String getTextContent() {
        return textContent;
    }

    public Attachment getAttachment() {
        return attachment;
    }

    @Override
    public RequestType getType() {
        return RequestType.CREATE_COMMENT;
    }
}
