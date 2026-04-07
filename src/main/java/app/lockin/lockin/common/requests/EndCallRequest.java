package app.lockin.lockin.common.requests;

public final class EndCallRequest extends Request {
    private final String callId;

    public EndCallRequest(String callId) {
        this.callId = callId;
    }

    public String getCallId() {
        return callId;
    }

    @Override
    public RequestType getType() {
        return RequestType.END_CALL;
    }
}
