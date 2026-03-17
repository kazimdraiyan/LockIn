package app.lockin.lockin.client.controller;
import app.lockin.lockin.client.model.Chat;
import app.lockin.lockin.client.model.ChatCell;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;

import java.util.Comparator;

public class ChatController {
    @FXML
    private ListView<Chat> chatListView;

    private ObservableList<Chat> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Populate Data
        masterData.addAll(
                new Chat("Abid",        "Mama Ghumacchilam",    3,  "2m",        Color.web("#12C4A3"), 2),
                new Chat("Atanu",       "You: Gay",             0,  "15m",       Color.web("#F5B041"), 15),
                new Chat("Tahsinul",    "You: kys nigga",       0,  "1h",        Color.web("#E74C3C"), 60),
                new Chat("Farreed",     "Baggy Jeans",          1,  "3h",        Color.web("#9B59B6"), 180),
                new Chat("Ikra",        "Goon",                 0,  "Yesterday", Color.web("#2E86C1"), 1440),
                new Chat("LockIn Team", "Kazi: Scat khabo mama",0,  "Mon",       Color.web("#F5B041"), 2880)
        );

        SortedList<Chat> sortedData = new SortedList<>(masterData,
                Comparator.comparingLong(Chat::getTimeValue));

        // Link List to Data
        chatListView.setItems(masterData);
        chatListView.setCellFactory(lv -> new ChatCell());

        // Handle Selection (Clicking a chat)
        chatListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                System.out.println("Opening chat with: " + newVal.getUserName());
                newVal.setUnreadCount(0); // Example of dynamic update
            }
        });
    }
}
