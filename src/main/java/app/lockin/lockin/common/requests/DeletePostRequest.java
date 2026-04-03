package app.lockin.lockin.common.requests;

public class DeletePostRequest extends Request {
    private final String postId;

    public DeletePostRequest(String postId) {
        this.postId = postId;
    }

    public String getPostId() {
        return postId;
    }

    @Override
    public RequestType getType() {
        return RequestType.DELETE_POST;
    }
}
