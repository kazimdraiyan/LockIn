package app.lockin.lockin.client.controllers;

import app.lockin.lockin.client.MyApplication;
import app.lockin.lockin.client.elements.ChatCell;
import app.lockin.lockin.client.models.ChatListItem;
import app.lockin.lockin.client.utils.TextFormatter;
import app.lockin.lockin.common.models.Chat;
import app.lockin.lockin.common.models.Message;
import app.lockin.lockin.common.models.MessageDelivery;
import app.lockin.lockin.common.requests.FetchRequest;
import app.lockin.lockin.common.requests.FetchType;
import app.lockin.lockin.common.response.Response;
import app.lockin.lockin.common.response.ResponseStatus;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;

public class ChatsController {
    @FXML
    private ListView<ChatListItem> chatListView;

    @FXML
    private SearchBarController searchBarController;

    private final ObservableList<ChatListItem> masterData = FXCollections.observableArrayList(item -> new Observable[]{
            item.sortTimestampProperty(),
            item.userNameProperty(),
            item.lastMessageProperty(),
            item.unreadCountProperty(),
            item.timeAgoProperty()
    });
    private MessengerController messengerController;
    private String pendingConversationUsername;

    @FXML
    public void initialize() {
        searchBarController.setPromptText("Search Messenger");
        searchBarController.getInputField().getParent().getStyleClass().add("search-bar-sidebar");

        FilteredList<ChatListItem> filteredData = new FilteredList<>(masterData, ignored -> true);
        searchBarController.getInputField().textProperty().addListener((obs, oldVal, newVal) -> {
            filteredData.setPredicate(chatListItem -> {
                if (newVal == null || newVal.isBlank()) {
                    return true;
                }
                String lower = newVal.toLowerCase(Locale.ENGLISH);
                return chatListItem.getUserName().toLowerCase(Locale.ENGLISH).contains(lower)
                        || chatListItem.getLastMessage().toLowerCase(Locale.ENGLISH).contains(lower);
            });
        });

        SortedList<ChatListItem> sortedData = new SortedList<>(filteredData, Comparator
                .comparingLong(ChatListItem::getSortTimestamp)
                .reversed()
                .thenComparing(item -> item.getUserName().toLowerCase(Locale.ENGLISH)));

        chatListView.setItems(sortedData);
        chatListView.setCellFactory(lv -> new ChatCell());
        chatListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                newVal.setUnreadCount(0);
                String activeConversation = messengerController == null ? null : messengerController.getActiveConversationUsername();
                if (activeConversation == null || !activeConversation.equals(newVal.getUserName())) {
                    loadConversation(newVal);
                }
                chatListView.refresh();
            }
        });
    }

    public void setMessengerController(MessengerController messengerController) {
        this.messengerController = messengerController;
        refreshChats();
    }

    public void applyMessageDelivery(MessageDelivery delivery, String activeChatUsername) {
        if (delivery == null || delivery.getChat() == null || delivery.getMessage() == null) {
            return;
        }

        ChatListItem item = findChatItem(delivery.getChat().getName());
        if (item == null) {
            item = buildChatListItem(delivery.getChat());
            masterData.add(item);
        }

        item.setChat(delivery.getChat());
        item.setLastMessage(buildPreview(delivery.getMessage()));
        item.setTimeAgo(formatTimeAgo(delivery.getMessage()));
        item.setSortTimestamp(delivery.getMessage().getCreatedAt());

        String currentUsername = MyApplication.clientManager.getAuthenticatedUsername();
        boolean incomingMessage = currentUsername != null && !currentUsername.equals(delivery.getMessage().getSenderUsername());
        boolean inactiveConversation = activeChatUsername == null || !delivery.getChat().getName().equals(activeChatUsername);
        item.setUnreadCount(incomingMessage && inactiveConversation ? item.getUnreadCount() + 1 : 0);
        chatListView.refresh();
    }

    public void markConversationOpen(String chatUsername) {
        ChatListItem item = findChatItem(chatUsername);
        if (item == null) {
            pendingConversationUsername = chatUsername;
            return;
        }

        pendingConversationUsername = null;
        item.setUnreadCount(0);
        chatListView.getSelectionModel().select(item);
        chatListView.refresh();
    }

    private void refreshChats() {
        new Thread(() -> {
            try {
                Response response = MyApplication.clientManager.sendRequest(new FetchRequest(FetchType.CHATS));
                if (response == null || response.getStatus() != ResponseStatus.SUCCESS) {
                    return;
                }

                @SuppressWarnings("unchecked")
                ArrayList<Chat> chats = (ArrayList<Chat>) response.getData();
                ArrayList<ChatListItem> items = new ArrayList<>();
                for (Chat chat : chats) {
                    items.add(buildChatListItem(chat));
                }

                Platform.runLater(() -> {
                    masterData.setAll(items);
                    if (pendingConversationUsername != null && !pendingConversationUsername.isBlank()) {
                        markConversationOpen(pendingConversationUsername);
                    }
                });
            } catch (IOException ignored) {
            }
        }).start();
    }

    private ChatListItem buildChatListItem(Chat chat) {
        Message lastMessage = chat.getLastMessage();
        return new ChatListItem(
                chat,
                chat.getName(),
                buildPreview(lastMessage),
                chat.getUnreadCount(),
                lastMessage == null ? "" : formatTimeAgo(lastMessage),
                lastMessage == null ? 0L : lastMessage.getCreatedAt()
        );
    }

    private void loadConversation(ChatListItem chatListItem) {
        if (messengerController != null) {
            messengerController.openConversation(chatListItem.getChat());
        }
    }

    private ChatListItem findChatItem(String username) {
        for (ChatListItem item : masterData) {
            if (item.getUserName().equals(username)) {
                return item;
            }
        }
        return null;
    }

    private String buildPreview(Message message) {
        if (message == null) {
            return "No messages yet";
        }
        if (message.getText() != null && !message.getText().isBlank()) {
            return message.getText();
        }
        if (message.getAttachment() != null && message.getAttachment().getMimeType() != null
                && message.getAttachment().getMimeType().startsWith("image/")) {
            return "Image";
        }
        if (message.getAttachment() != null && message.getAttachment().getOriginalFileName() != null) {
            return message.getAttachment().getOriginalFileName();
        }
        return "Message";
    }

    private String formatTimeAgo(Message message) {
        return TextFormatter.formatTimestamp(message.getCreatedAt());
    }
}
