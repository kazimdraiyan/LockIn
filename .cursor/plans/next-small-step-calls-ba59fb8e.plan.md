<!-- ba59fb8e-1435-42c7-afd5-63d4fe2ab3b4 -->
---
todos:
  - id: "add-client-call-methods"
    content: "Add startCall and answerCall methods to ClientManager using existing request classes"
    status: pending
  - id: "keep-type-driven-dispatch"
    content: "Keep dispatchResponse call routing based on CallSignal payload only"
    status: pending
  - id: "verify-call-flow"
    content: "Compile and validate caller/callee signal sequence in a quick manual test"
    status: pending
isProject: false
---
# Next Small Step: Client Call API

## Goal
Expose tiny, direct methods on `ClientManager` to trigger call signaling without touching UI architecture.

## Why this step
- `CallHandler` and request classes already exist and work.
- `dispatchResponse` already forwards all successful `CallSignal` payloads.
- The missing piece is easy call initiation/answering entry points from the client side.

## Plan
- Add `startCall(String calleeUsername)` in `ClientManager` that calls `sendRequest(new StartCallRequest(calleeUsername))`.
- Add `answerCall(String callId, boolean accept)` in `ClientManager` that calls `sendRequest(new AnswerCallRequest(callId, accept))`.
- Keep methods minimal: no wrappers, no extra model classes, no UI coupling.
- Keep current `CallSignal` payload usage unchanged (`type`, `callId`, usernames, `accepted`).
- Verify with compile and a manual 2-client flow:
  - Caller sends start-call
  - Callee receives `CallSignalType.INCOMING`
  - Caller receives `CallSignalType.RINGING`
  - Callee answers
  - Caller receives `CallSignalType.ANSWERED`

## Files to touch
- `src/main/java/app/lockin/lockin/client/ClientManager.java`

## Out of scope
- End-call, busy state, multi-device ring-stop sync, audio/UDP media relay, encryption.