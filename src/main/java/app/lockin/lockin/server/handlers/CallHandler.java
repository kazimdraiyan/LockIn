package app.lockin.lockin.server.handlers;

import app.lockin.lockin.common.models.CallSignal;
import app.lockin.lockin.common.models.CallSignalType;
import app.lockin.lockin.common.requests.AnswerCallRequest;
import app.lockin.lockin.common.requests.EndCallRequest;
import app.lockin.lockin.common.requests.StartCallRequest;
import app.lockin.lockin.common.response.Response;
import app.lockin.lockin.common.response.ResponseStatus;
import app.lockin.lockin.server.services.ConnectedClientRegistry;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class CallHandler {
    private static final ConcurrentHashMap<String, CallSignal> PENDING_CALLS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, CallSignal> ACTIVE_CALLS = new ConcurrentHashMap<>();

    public Response handleStartCall(StartCallRequest request) {
        if (request.authenticatedSession == null) {
            return new Response(ResponseStatus.ERROR, "Not authenticated", null);
        }
        String caller = request.authenticatedSession.getUsername();
        String callee = request.getCalleeUsername();
        if (callee == null || callee.isBlank()) {
            return new Response(ResponseStatus.ERROR, "Callee username required", null);
        }
        if (caller.equals(callee)) {
            return new Response(ResponseStatus.ERROR, "Cannot call yourself", null);
        }

        ArrayList<ClientHandler> calleeClients = ConnectedClientRegistry.getClients(callee);
        if (calleeClients.isEmpty()) {
            return new Response(ResponseStatus.ERROR, request.getCalleeUsername() + " is offline", null);
        }

        String callId = UUID.randomUUID().toString();
        PENDING_CALLS.put(callId, new CallSignal(CallSignalType.PENDING, callId, caller, callee, null));

        CallSignal incoming = new CallSignal(CallSignalType.INCOMING, callId, caller, callee, null);
        Response incomingResponse = new Response(ResponseStatus.SUCCESS, "Incoming call", incoming);
        for (ClientHandler client : calleeClients) {
            client.send(incomingResponse); // Ring every device of the callee
        }
        return new Response(ResponseStatus.SUCCESS, "Call ringing", new CallSignal(CallSignalType.RINGING, callId, caller, callee, null)); // Caller gets this response after starting the call
    }

    public Response handleAnswerCall(AnswerCallRequest request) {
        if (request.authenticatedSession == null) {
            return new Response(ResponseStatus.ERROR, "Not authenticated", null);
        }
        String callee = request.authenticatedSession.getUsername();
        String callId = request.getCallId();
        if (callId == null || callId.isBlank()) {
            return new Response(ResponseStatus.ERROR, "Call id required", null);
        }

        CallSignal pending = PENDING_CALLS.remove(callId);
        if (pending == null) {
            return new Response(ResponseStatus.ERROR, "No such call", null);
        }
        if (!callee.equals(pending.getCalleeUsername())) {
            PENDING_CALLS.put(callId, pending);
            return new Response(ResponseStatus.ERROR, "Not your incoming call", null);
        }

        CallSignal answered = new CallSignal(CallSignalType.ANSWERED, callId, pending.getCallerUsername(), pending.getCalleeUsername(), request.isAccept());
        if (request.isAccept()) {
            ACTIVE_CALLS.put(callId, new CallSignal(CallSignalType.PENDING, callId, pending.getCallerUsername(), pending.getCalleeUsername(), null));
        }
        Response callerNotification = new Response(ResponseStatus.SUCCESS, "Call answered", answered);
        for (ClientHandler client : ConnectedClientRegistry.getClients(pending.getCallerUsername())) {
            client.send(callerNotification);
        }
        Response calleeNotification = new Response(ResponseStatus.SUCCESS, "Call answered", answered);
        for (ClientHandler client : ConnectedClientRegistry.getClients(pending.getCalleeUsername())) {
            client.send(calleeNotification);
        }

        String message = request.isAccept() ? "Call accepted" : "Call rejected";
        return new Response(ResponseStatus.SUCCESS, message, null); // Callee gets this response after answering the call
    }

    public Response handleEndCall(EndCallRequest request) {
        if (request.authenticatedSession == null) {
            return new Response(ResponseStatus.ERROR, "Not authenticated", null);
        }
        String requester = request.authenticatedSession.getUsername();
        String callId = request.getCallId();
        if (callId == null || callId.isBlank()) {
            return new Response(ResponseStatus.ERROR, "Call id required", null);
        }

        CallSignal call = ACTIVE_CALLS.remove(callId);
        boolean wasActive = true;
        if (call == null) {
            call = PENDING_CALLS.remove(callId);
            wasActive = false;
        }
        if (call == null) {
            return new Response(ResponseStatus.ERROR, "No such call", null);
        }

        if (!requester.equals(call.getCallerUsername()) && !requester.equals(call.getCalleeUsername())) {
            if (wasActive) {
                ACTIVE_CALLS.put(callId, call);
            } else {
                PENDING_CALLS.put(callId, call);
            }
            return new Response(ResponseStatus.ERROR, "Not your call", null);
        }

        CallSignal ended = new CallSignal(CallSignalType.ENDED, callId, call.getCallerUsername(), call.getCalleeUsername(), null);
        Response endedResponse = new Response(ResponseStatus.SUCCESS, "Call ended", ended);
        for (ClientHandler client : ConnectedClientRegistry.getClients(call.getCallerUsername())) {
            client.send(endedResponse);
        }
        for (ClientHandler client : ConnectedClientRegistry.getClients(call.getCalleeUsername())) {
            client.send(endedResponse);
        }
        return new Response(ResponseStatus.SUCCESS, "Call ended", null);
    }

    public String peerInActiveCall(String callId, String username) {
        if (callId == null || callId.isBlank() || username == null || username.isBlank()) {
            return null;
        }
        CallSignal active = ACTIVE_CALLS.get(callId);
        if (active == null) {
            return null;
        }
        if (username.equals(active.getCallerUsername())) {
            return active.getCalleeUsername();
        }
        if (username.equals(active.getCalleeUsername())) {
            return active.getCallerUsername();
        }
        return null;
    }
}
