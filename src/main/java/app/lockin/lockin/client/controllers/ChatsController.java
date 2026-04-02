package app.lockin.lockin.client.controllers;

import app.lockin.lockin.client.MyApplication;
import app.lockin.lockin.client.elements.ChatCell;
import app.lockin.lockin.client.models.ChatListItem;
import app.lockin.lockin.client.utils.TextFormatter;
import app.lockin.lockin.common.models.Chat;
import app.lockin.lockin.common.requests.FetchRequest;
import app.lockin.lockin.common.requests.FetchType;
import app.lockin.lockin.common.response.Response;
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

public class ChatsController {
    @FXML
    private ListView<ChatListItem> chatListView;

    @FXML
    private SearchBarController searchBarController;

    private ObservableList<ChatListItem> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() throws IOException {
        searchBarController.setPromptText("Search Messenger");
        searchBarController.getInputField().getParent().getStyleClass().add("search-bar-sidebar");

        MyApplication.clientManager.send(new FetchRequest(FetchType.CHATS));
        Response response = MyApplication.clientManager.receive();
        ArrayList<Chat> chats = (ArrayList<Chat>) response.getData();
        for (Chat chat : chats) {
            String lastMessage = chat.getLastMessage() == null ? "No messages" : chat.getLastMessage().getText();
            Instant timestamp = chat.getLastMessage() == null ? Instant.now() : chat.getLastMessage().getTimestamp();
            Duration timeAgo = Duration.between(timestamp, Instant.now());
            masterData.addAll(new ChatListItem(chat.getName(), lastMessage, chat.getUnreadCount(), TextFormatter.readableDuration(timeAgo)));
        }

        // Placeholder data
        // TODO: Add real data from server
//        masterData.addAll(
//                new ChatListItem("Abid", "Mama Ghumacchilam", 3, 2),
//                new ChatListItem("Atanu", "You: Gay", 0, 15),
//                new ChatListItem("Tahsinul", "You: kys nigga", 0, 60),
//                new ChatListItem("Farreed", "Baggy Jeans", 1, 180),
//                new ChatListItem("Ikra", "Goon", 0, 1440),
//                new ChatListItem("LockIn Team", "Kazi: Scat khabo mama", 0, 2880)
//        );

        FilteredList<ChatListItem> filteredData = new FilteredList<>(masterData, p -> true); // Mapped ObservableList to FilteredList

        // Handle search
        searchBarController.getInputField().textProperty().addListener((obs, oldVal, newVal) -> {
            System.out.println("Searching for: " + newVal);
            filteredData.setPredicate(chatListItem -> {
                if (newVal == null || newVal.isBlank()) return true;
                String lower = newVal.toLowerCase();
                return chatListItem.getUserName().toLowerCase().contains(lower)
                        || chatListItem.getLastMessage().toLowerCase().contains(lower);
            });
        });

        // Sort by last sent message
//        SortedList<ChatListItem> sortedData = new SortedList<>(masterData,
//                Comparator.comparing(ChatListItem::getTimeAgo));
        SortedList<ChatListItem> sortedData = new SortedList<>(masterData);

        // Link List to Data
        chatListView.setItems(sortedData);
        chatListView.setCellFactory(lv -> new ChatCell());

        // Handle Selection (Clicking a chat)
        chatListView.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        newVal.setUnreadCount(0);
                        loadConversation(newVal);
                    }
                });
    }

    private void loadConversation(ChatListItem chatListItem) {
        // TODO: Implement conversation loading
        System.out.println(chatListItem.getUserName() + " is clicked");
    }
}
