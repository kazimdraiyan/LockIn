package app.lockin.lockin.common.models;

import java.time.Instant;

public class ImageMessage extends Message {
    String imageID;

    public ImageMessage(String imageID, String senderUsername, Instant timestamp) {
        super(senderUsername, timestamp);
        this.imageID = imageID;
    }

    @Override
    MessageType getType() {
        return MessageType.IMAGE;
    }

    @Override
    public String getText() {
        return "Photo";
    }
}
