package app.lockin.lockin.common.models;

import java.io.Serializable;

// TODO: Merge MessageAttachment with PostAttachment
public class MessageAttachment implements Serializable {
    private final String originalFileName;
    private final String mimeType; // Content type and subtype
    private final byte[] data; // TODO: Learn more about byte array

    public MessageAttachment(String originalFileName, String mimeType, byte[] data) {
        this.originalFileName = originalFileName;
        this.mimeType = mimeType;
        this.data = data;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public byte[] getData() {
        return data;
    }
}
