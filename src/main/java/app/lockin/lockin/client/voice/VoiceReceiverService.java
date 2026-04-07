package app.lockin.lockin.client.voice;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public final class VoiceReceiverService {
    private final ArrayBlockingQueue<byte[]> frameQueue;
    private volatile String activeCallId;
    private int receivedFrameCount;

    public VoiceReceiverService(int queueCapacity) {
        frameQueue = new ArrayBlockingQueue<>(queueCapacity);
    }

    public void setActiveCallId(String activeCallId) {
        this.activeCallId = activeCallId;
        frameQueue.clear();
        receivedFrameCount = 0;
        System.out.println("VOICE RECV activeCallId=" + activeCallId);
    }

    public void onVoiceFrame(String callId, byte[] frame) {
        if (frame == null || frame.length == 0) {
            return;
        }
        String currentCall = activeCallId;
        if (currentCall == null || !currentCall.equals(callId)) {
            return;
        }
        receivedFrameCount++;
        if (receivedFrameCount % 100 == 0) {
            System.out.println("VOICE RECV frames=" + receivedFrameCount + " callId=" + callId + " queue=" + frameQueue.size());
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
