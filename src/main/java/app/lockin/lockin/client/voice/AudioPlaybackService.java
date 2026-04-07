package app.lockin.lockin.client.voice;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public final class AudioPlaybackService {
    private final VoiceReceiverService receiverService;
    private final AudioFormat audioFormat;
    private final int frameSizeBytes;

    private volatile boolean running;
    private volatile SourceDataLine sourceLine;
    private int playedFrameCount;

    public AudioPlaybackService(VoiceReceiverService receiverService, AudioFormat audioFormat, int frameSizeBytes) {
        this.receiverService = receiverService;
        this.audioFormat = audioFormat;
        this.frameSizeBytes = frameSizeBytes;
    }

    public void start() throws LineUnavailableException {
        if (running) {
            return;
        }
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        SourceDataLine line = (SourceDataLine) javax.sound.sampled.AudioSystem.getLine(info);
        line.open(audioFormat);
        line.start();
        sourceLine = line;
        running = true;
        playedFrameCount = 0;
        System.out.println("VOICE PLAY start frameBytes=" + frameSizeBytes);

        Thread thread = new Thread(this::playLoop, "lockin-audio-playback");
        thread.setDaemon(true);
        thread.start();
    }

    public void stop() {
        running = false;
        SourceDataLine line = sourceLine;
        if (line != null) {
            line.stop();
            line.close();
            sourceLine = null;
        }
        System.out.println("VOICE PLAY stop frames=" + playedFrameCount);
    }

    private void playLoop() {
        byte[] silence = new byte[frameSizeBytes];
        while (running) {
            try {
                byte[] frame = receiverService.pollFrame(25);
                if (frame == null) {
                    sourceLine.write(silence, 0, silence.length);
                    continue;
                }
                sourceLine.write(frame, 0, frame.length);
                playedFrameCount++;
                if (playedFrameCount % 100 == 0) {
                    System.out.println("VOICE PLAY frames=" + playedFrameCount);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
