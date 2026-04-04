package app.lockin.lockin.common.requests;

public class FetchRequest extends Request {
    private final FetchType fetchType;
    private final String query;

    public FetchRequest(FetchType fetchType) {
        this(fetchType, null);
    }

    public FetchRequest(FetchType fetchType, String query) {
        this.fetchType = fetchType;
        this.query = query;
    }

    public FetchType getFetchType() {
        return fetchType;
    }

    public String getQuery() {
        return query;
    }

    @Override
    public RequestType getType() {
        return RequestType.FETCH;
    }
}
