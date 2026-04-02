package app.lockin.lockin.common.requests;

import app.lockin.lockin.common.models.PostAttachment;

public class CreateCommentRequest extends Request {
    private final String postId;
    private final String textContent;
    private final PostAttachment attachment;

    public CreateCommentRequest(String postId, String textContent, PostAttachment attachment) {
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

    public PostAttachment getAttachment() {
        return attachment;
    }

    @Override
    public RequestType getType() {
        return RequestType.CREATE_COMMENT;
    }
}
