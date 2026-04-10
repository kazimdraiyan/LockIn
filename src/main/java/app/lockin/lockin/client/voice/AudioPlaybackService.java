package app.lockin.lockin.client.voice;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public final class AudioPlaybackService {
    private final VoiceReceiverService receiverService;
    private final AudioFormat audioFormat;
    private final int frameSizeBytes;

    private volatile boolean running;
    private volatile SourceDataLine sourceLine;

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
        applyMaxMasterGain(line);
        line.start();
        sourceLine = line;
        running = true;

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
    }

    private static void applyMaxMasterGain(SourceDataLine line) {
        if (!line.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            return;
        }
        FloatControl gain = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
        gain.setValue(gain.getMaximum());
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
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
