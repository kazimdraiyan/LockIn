package app.lockin.lockin.common.models;

import java.io.Serializable;

public class Attachment implements Serializable {
    private final String originalFileName;
    private final String mimeType;
    private final byte[] data;

    public Attachment(String originalFileName, String mimeType, byte[] data) {
        this.originalFileName = originalFileName;
        this.mimeType = mimeType;
        this.data = data == null ? new byte[0] : data.clone();
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public byte[] getData() {
        return data.clone();
    }
}
