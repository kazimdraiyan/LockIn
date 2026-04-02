package app.lockin.lockin.common.models;

import java.io.Serializable;

public class Chat implements Serializable {
    String name;
    Message lastMessage;
    int unreadCount;

    public Chat(String name) {
        this.name = name;
        this.lastMessage = null; // Keeping it null for simplicity // TODO: Change this
        this.unreadCount = 0;
    }

    public String getName() {
        return name;
    }

    public Message getLastMessage() {
        return lastMessage;
    }

    public int getUnreadCount() {
        return unreadCount;
    }
}
