package app.lockin.lockin.client.voice;

import app.lockin.lockin.client.UdpClient;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import java.io.IOException;

public final class VoiceSenderService {
    private final UdpClient udpClient;
    private final AudioFormat audioFormat;
    private final int frameSizeBytes;

    private volatile boolean running;
    private volatile String activeCallId;
    private volatile TargetDataLine targetLine;

    public VoiceSenderService(UdpClient udpClient, AudioFormat audioFormat, int frameSizeBytes) {
        this.udpClient = udpClient;
        this.audioFormat = audioFormat;
        this.frameSizeBytes = frameSizeBytes;
    }

    public void start(String callId) throws LineUnavailableException {
        if (running) {
            return;
        }
        if (callId == null || callId.isBlank()) {
            throw new IllegalArgumentException("Call id required");
        }
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);
        TargetDataLine line = (TargetDataLine) javax.sound.sampled.AudioSystem.getLine(info);
        line.open(audioFormat);
        line.start();
        targetLine = line;
        activeCallId = callId;
        running = true;

        Thread thread = new Thread(this::captureLoop, "lockin-audio-capture");
        thread.setDaemon(true);
        thread.start();
    }

    public void stop() {
        running = false;
        activeCallId = null;
        TargetDataLine line = targetLine;
        if (line != null) {
            line.stop();
            line.close();
            targetLine = null;
        }
    }

    private void captureLoop() {
        byte[] frame = new byte[frameSizeBytes];
        while (running) {
            TargetDataLine line = targetLine;
            String callId = activeCallId;
            if (line == null || callId == null) {
                break;
            }
            int read = line.read(frame, 0, frame.length);
            if (read <= 0) {
                continue;
            }
            byte[] payload = new byte[read];
            System.arraycopy(frame, 0, payload, 0, read);
            try {
                udpClient.sendVoiceFrame(callId, payload);
            } catch (IOException e) {
                if (running) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }
}
