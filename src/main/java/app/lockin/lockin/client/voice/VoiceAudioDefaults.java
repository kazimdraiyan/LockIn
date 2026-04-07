package app.lockin.lockin.client.voice;

import javax.sound.sampled.AudioFormat;

public final class VoiceAudioDefaults {
    public static final float SAMPLE_RATE = 16000.0f;
    public static final int CHANNELS = 1;
    public static final int SAMPLE_SIZE_BITS = 16;
    public static final int FRAME_MILLIS = 20;
    public static final int FRAME_SIZE_BYTES = (int) (SAMPLE_RATE * (SAMPLE_SIZE_BITS / 8.0f) * CHANNELS * (FRAME_MILLIS / 1000.0f));
    public static final int RECEIVE_QUEUE_CAPACITY = 5;

    private VoiceAudioDefaults() {
    }

    public static AudioFormat audioFormat() {
        return new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE_BITS, CHANNELS, true, false);
    }
}
