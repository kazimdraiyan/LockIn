package app.lockin.lockin.client.models;
import javafx.beans.property.*;
import javafx.scene.paint.Color;

// TODO: Clean this. Current plan: client/model/ should contain only UI models. Whereas, common/model/ should contain data models
public class Chat {
    private final StringProperty userName = new SimpleStringProperty();
    private final StringProperty lastMessage = new SimpleStringProperty();
    private final IntegerProperty unreadCount = new SimpleIntegerProperty();
    private final StringProperty timestamp = new SimpleStringProperty();
    private final ObjectProperty<Color> avatarColor = new SimpleObjectProperty<>();
    private final LongProperty timeValue = new SimpleLongProperty();

    public Chat(String name, String message, int unread, String time, Color color, long timeMinutes) {
        setUserName(name);
        setLastMessage(message);
        setUnreadCount(unread);
        setTimestamp(time);
        setAvatarColor(color);
        setTimeValue(timeMinutes);
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

    // --- Timestamp Property ---
    public String getTimestamp() { return timestamp.get(); }
    public void setTimestamp(String time) { this.timestamp.set(time); }
    public StringProperty timestampProperty() { return timestamp; }

    // --- AvatarColor Property ---
    public Color getAvatarColor() { return avatarColor.get(); }
    public void setAvatarColor(Color color) { this.avatarColor.set(color); }
    public ObjectProperty<Color> avatarColorProperty() { return avatarColor; }

    public long getTimeValue() { return timeValue.get(); }
    public void setTimeValue(long val) { this.timeValue.set(val); }
    public LongProperty timeValueProperty() { return timeValue; }
}
