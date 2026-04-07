package app.lockin.lockin.common.requests;

public final class StartCallRequest extends Request {
    private final String calleeUsername;

    public StartCallRequest(String calleeUsername) {
        this.calleeUsername = calleeUsername;
    }

    public String getCalleeUsername() {
        return calleeUsername;
    }

    @Override
    public RequestType getType() {
        return RequestType.START_CALL;
    }
}
