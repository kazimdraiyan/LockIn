package app.lockin.lockin.common.models;

import java.io.Serializable;

public class Chat implements Serializable {
    String name;
    Message lastMessage;

    public Chat(String name) {
        this.name = name;
        this.lastMessage = null; // Keeping it null for simplicity // TODO: Change this
    }

    public String getName() {
        return name;
    }

    public Message getLastMessage() {
        return lastMessage;
    }
}
