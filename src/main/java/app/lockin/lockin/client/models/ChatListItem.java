package app.lockin.lockin.client.models;
import javafx.beans.property.*;

// TODO: Clean this. Current plan: client/model/ should contain only UI models. Whereas, common/model/ should contain data models
public class ChatListItem {
    private final StringProperty userName = new SimpleStringProperty();
    private final StringProperty lastMessage = new SimpleStringProperty();
    private final IntegerProperty unreadCount = new SimpleIntegerProperty();
    private final StringProperty timeAgo = new SimpleStringProperty();

    public ChatListItem(String name, String message, int unread, String timeAgo) {
        setUserName(name);
        setLastMessage(message);
        setUnreadCount(unread);
        setTimeAgo(timeAgo);
    }

    // --- UserName Property ---
    public String getUserName() { return userName.get(); }
    public void setUserName(String name) { this.userName.set(name); }
    public StringProperty userNameProperty() { return userName; }

    // --- LastMessage Property ---
    public String getLastMessage() { return lastMessage.get(); }
    public void setLastMessage(String msg) { this.lastMessage.set(msg); }
    public StringProperty lastMessageProperty() { return lastMessage; }

    // --- UnreadCount Property ---
    public int getUnreadCount() { return unreadCount.get(); }
    public void setUnreadCount(int count) { this.unreadCount.set(count); }
    public IntegerProperty unreadCountProperty() { return unreadCount; }

    public String getTimeAgo() { return timeAgo.get(); }
    public void setTimeAgo(String val) { this.timeAgo.set(val); }
    public StringProperty timeAgoProperty() { return timeAgo; }
}
