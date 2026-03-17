package app.lockin.lockin.server.model;

public class TextMessage extends Message {
    String text;

    public TextMessage(String text) {
        this.text = text;
    }

    @Override
    MessageType getType() {
        return MessageType.TEXT;
    }
}
