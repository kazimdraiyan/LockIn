package app.lockin.lockin.common.requests;

public class FetchMessagesRequest extends Request {
    private final String otherUsername; // TODO: Generalize for including group chats

    public FetchMessagesRequest(String otherUsername) {
        this.otherUsername = otherUsername;
    }

    public String getOtherUsername() {
        return otherUsername;
    }

    @Override
    public RequestType getType() {
        return RequestType.FETCH_MESSAGES;
    }
}
