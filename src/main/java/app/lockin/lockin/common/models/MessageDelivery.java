package app.lockin.lockin.common.models;

import java.io.Serializable;

public class MessageDelivery implements Serializable {
    private final Chat chat;
    private final Message message;

    public MessageDelivery(Chat chat, Message message) {
        this.chat = chat;
        this.message = message;
    }

    public Chat getChat() {
        return chat;
    }

    public Message getMessage() {
        return message;
    }
}
