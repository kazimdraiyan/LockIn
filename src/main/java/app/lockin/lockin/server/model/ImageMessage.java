package app.lockin.lockin.server.model;

public class ImageMessage extends Message {
    @Override
    MessageType getType() {
        return MessageType.IMAGE;
    }
}
