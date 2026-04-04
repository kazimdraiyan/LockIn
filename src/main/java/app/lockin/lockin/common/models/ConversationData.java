package app.lockin.lockin.common.models;

import java.io.Serializable;
import java.util.ArrayList;

public class ConversationData implements Serializable {
    private final Chat chat;
    private final ArrayList<Message> messages;

    public ConversationData(Chat chat, ArrayList<Message> messages) {
        this.chat = chat;
        this.messages = messages;
    }

    public Chat getChat() {
        return chat;
    }

    public ArrayList<Message> getMessages() {
        return messages;
    }
}
