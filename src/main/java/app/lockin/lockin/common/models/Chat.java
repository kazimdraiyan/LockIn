package app.lockin.lockin.common.models;

import java.io.Serializable;

public class Chat implements Serializable {
    private final String id;
    private final String name;
    private final Message lastMessage;
    private final int unreadCount;

    public Chat(String id, String name, Message lastMessage, int unreadCount) {
        this.id = id;
        this.name = name;
        this.lastMessage = lastMessage;
        this.unreadCount = unreadCount;
    }

    public Chat(String name) {
        this(null, name, null, 0);
    }

    public String getId() {
        return id;
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
