package app.lockin.lockin.common.models;

import java.io.Serializable;
import java.time.Instant;

public abstract class Message implements Serializable {
    MessageType type;
    String senderUsername;
    Instant timestamp;

    public Message(String senderUsername, Instant timestamp) {
        this.senderUsername = senderUsername;
        this.timestamp = timestamp;
    }

    abstract MessageType getType();

    public abstract String getText();

    public Instant getTimestamp() {
        return timestamp;
    }
}
