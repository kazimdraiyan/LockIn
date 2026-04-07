package app.lockin.lockin.common.requests;

public final class AnswerCallRequest extends Request {
    private final String callId; // TODO: Add CallSignal instead of callId?
    private final boolean accept;

    public AnswerCallRequest(String callId, boolean accept) {
        this.callId = callId;
        this.accept = accept;
    }

    public String getCallId() {
        return callId;
    }

    public boolean isAccept() {
        return accept;
    }

    @Override
    public RequestType getType() {
        return RequestType.ANSWER_CALL;
    }
}
