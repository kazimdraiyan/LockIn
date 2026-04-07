package app.lockin.lockin.client.voice;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public final class VoiceReceiverService {
    private final ArrayBlockingQueue<byte[]> frameQueue;
    private volatile String activeCallId;

    public VoiceReceiverService(int queueCapacity) {
        frameQueue = new ArrayBlockingQueue<>(queueCapacity);
    }

    public void setActiveCallId(String activeCallId) {
        this.activeCallId = activeCallId;
        frameQueue.clear();
    }

    public void onVoiceFrame(String callId, byte[] frame) {
        if (frame == null || frame.length == 0) {
            return;
        }
        String currentCall = activeCallId;
        if (currentCall == null || !currentCall.equals(callId)) {
            return;
        }
        if (!frameQueue.offer(frame)) {
            // If adding new frame is not possible due to full-capacity, we pop a frame then add the new frame
            frameQueue.poll();
            frameQueue.offer(frame);
        }
    }

    public byte[] pollFrame(long timeoutMs) throws InterruptedException {
        return frameQueue.poll(timeoutMs, TimeUnit.MILLISECONDS);
    }
}
