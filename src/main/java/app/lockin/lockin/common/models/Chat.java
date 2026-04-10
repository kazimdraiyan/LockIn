package app.lockin.lockin.common.models;

import java.io.Serializable;

public class Chat implements Serializable {
    public static final String COMMON_CHAT_ID = "common_chat";
    public static final String COMMON_CHAT_NAME = "Common Chat";

    private final String id;
    private final String name;
    private final Message lastMessage;
    private final int unreadCount;
    private final Attachment profilePicture;

    public Chat(String id, String name, Message lastMessage, int unreadCount, Attachment profilePicture) {
        this.id = id;
        this.name = name;
        this.lastMessage = lastMessage;
        this.unreadCount = unreadCount;
        this.profilePicture = profilePicture;
    }

    public Chat(String id, String name, Message lastMessage, int unreadCount) {
        this(id, name, lastMessage, unreadCount, null);
    }

    public Chat(String name) {
        this(null, name, null, 0, null);
    }

    public boolean isCommonChat() {
        return COMMON_CHAT_ID.equals(id);
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

    public Attachment getProfilePicture() {
        return profilePicture;
    }
}
