package app.lockin.lockin.common.models;

import java.time.Instant;

public class TextMessage extends Message {
    String text;

    public TextMessage(String text, String senderUsername, Instant timestamp) {
        super(senderUsername, timestamp);
        this.text = text;
    }

    @Override
    MessageType getType() {
        return MessageType.TEXT;
    }

    @Override
    public String getText() {
        return text;
    }
}
