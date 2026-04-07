package app.lockin.lockin.common.models;

import java.io.Serializable;

// Push payloads for call flow: incoming (caller set), ringing (only callId), answered (accepted set). Pending state on server uses all caller/callee.
public final class CallSignal implements Serializable {
    private final CallSignalType type;
    private final String callId;
    private final String callerUsername;
    private final String calleeUsername;
    private final Boolean accepted; // null when incoming, ringing, or pending

    public CallSignal(CallSignalType type, String callId, String callerUsername, String calleeUsername, Boolean accepted) {
        this.type = type;
        this.callId = callId;
        this.callerUsername = callerUsername;
        this.calleeUsername = calleeUsername;
        this.accepted = accepted;
    }

    public CallSignalType getType() {
        return type;
    }

    public String getCallId() {
        return callId;
    }

    public String getCallerUsername() {
        return callerUsername;
    }

    public String getCalleeUsername() {
        return calleeUsername;
    }

    public Boolean getAccepted() {
        return accepted;
    }
}
