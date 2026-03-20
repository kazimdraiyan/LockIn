package app.lockin.lockin.client.controller;
import app.lockin.lockin.MyApplication;
import app.lockin.lockin.client.model.Chat;
import app.lockin.lockin.client.element.ChatCell;
import app.lockin.lockin.server.request.FetchRequest;
import app.lockin.lockin.server.request.FetchType;
import app.lockin.lockin.server.response.Response;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;

public class ChatsController {
    @FXML
    private ListView<Chat> chatListView;

    @FXML
    private TextField searchField;

    private ObservableList<Chat> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() throws IOException {
        // Placeholder data
        MyApplication.clientManager.send(new FetchRequest(FetchType.CHATS));
        Response response = MyApplication.clientManager.receive();
        ArrayList<app.lockin.lockin.server.model.Chat> chats = (ArrayList<app.lockin.lockin.server.model.Chat>) response.getData();
        for (app.lockin.lockin.server.model.Chat chat : chats) {
            System.out.println(chat.getName());
        }

        // TODO: Add real data from server
        masterData.addAll(
                new Chat("Abid",        "Mama Ghumacchilam",    3,  "2m",        Color.web("#12C4A3"), 2),
                new Chat("Atanu",       "You: Gay",             0,  "15m",       Color.web("#F5B041"), 15),
                new Chat("Tahsinul",    "You: kys nigga",       0,  "1h",        Color.web("#E74C3C"), 60),
                new Chat("Farreed",     "Baggy Jeans",          1,  "3h",        Color.web("#9B59B6"), 180),
                new Chat("Ikra",        "Goon",                 0,  "Yesterday", Color.web("#2E86C1"), 1440),
                new Chat("LockIn Team", "Kazi: Scat khabo mama",0,  "Mon",       Color.web("#F5B041"), 2880)
        );

        FilteredList<Chat> filteredData = new FilteredList<>(masterData, p -> true); // Mapped ObservableList to FilteredList

        // Handle search
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
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
