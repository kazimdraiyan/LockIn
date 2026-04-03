package app.lockin.lockin.common.requests;

// TODO: Split FetchRequest into FetchChatsRequest and FetchPostsRequest. Remove FetchType enum.
public class FetchRequest extends Request {
    private final FetchType fetchType;

    public FetchRequest(FetchType fetchType) {
        this.fetchType = fetchType;
    }

    public FetchType getFetchType() {
        return fetchType;
    }

    @Override
    public RequestType getType() {
        return RequestType.FETCH;
    }
}
