package app.lockin.lockin.client.controllers;
import app.lockin.lockin.client.MyApplication;
import app.lockin.lockin.client.models.Chat;
import app.lockin.lockin.client.elements.ChatCell;
import app.lockin.lockin.common.requests.FetchRequest;
import app.lockin.lockin.common.requests.FetchType;
import app.lockin.lockin.common.response.Response;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;

public class ChatsController {
    @FXML
    private ListView<Chat> chatListView;

    @FXML
    private SearchBarController searchBarController;

    private ObservableList<Chat> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() throws IOException {
        searchBarController.setPromptText("Search Messenger");
        searchBarController.getInputField().getParent().getStyleClass().add("search-bar-sidebar");

        MyApplication.clientManager.send(new FetchRequest(FetchType.CHATS));
        Response response = MyApplication.clientManager.receive();
        ArrayList<app.lockin.lockin.common.models.Chat> chats = (ArrayList<app.lockin.lockin.common.models.Chat>) response.getData();
        for (app.lockin.lockin.common.models.Chat chat : chats) {
            System.out.println(chat.getName());
        }

        // Placeholder data
        // TODO: Add real data from server
        masterData.addAll(
                new Chat("Abid",        "Mama Ghumacchilam",    3,  "2m",        2),
                new Chat("Atanu",       "You: Gay",             0,  "15m",       15),
                new Chat("Tahsinul",    "You: kys nigga",       0,  "1h",        60),
                new Chat("Farreed",     "Baggy Jeans",          1,  "3h",        180),
                new Chat("Ikra",        "Goon",                 0,  "Yesterday", 1440),
                new Chat("LockIn Team", "Kazi: Scat khabo mama",0,  "Mon",       2880)
        );

        FilteredList<Chat> filteredData = new FilteredList<>(masterData, p -> true); // Mapped ObservableList to FilteredList

        // Handle search
        searchBarController.getInputField().textProperty().addListener((obs, oldVal, newVal) -> {
            System.out.println("Searching for: " + newVal);
            filteredData.setPredicate(chat -> {
                if (newVal == null || newVal.isBlank()) return true;
                String lower = newVal.toLowerCase();
                return chat.getUserName().toLowerCase().contains(lower)
                        || chat.getLastMessage().toLowerCase().contains(lower);
            });
        });

        // Sort by last sent message
        SortedList<Chat> sortedData = new SortedList<>(masterData,
                Comparator.comparingLong(Chat::getTimeValue));

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

    private void loadConversation(Chat chat) {
        // TODO: Implement conversation loading
        System.out.println(chat.getUserName() + " is clicked");
    }
}
