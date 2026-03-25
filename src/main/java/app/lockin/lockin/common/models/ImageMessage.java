package app.lockin.lockin.common.models;

public class ImageMessage extends Message {
    @Override
    MessageType getType() {
        return MessageType.IMAGE;
    }
}
