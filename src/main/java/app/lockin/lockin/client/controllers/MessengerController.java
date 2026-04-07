package app.lockin.lockin.client.controllers;

import app.lockin.lockin.client.MyApplication;
import app.lockin.lockin.common.models.Chat;
import app.lockin.lockin.common.models.MessageDelivery;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;

import java.util.function.Consumer;

public class MessengerController implements MainControllerAware {
    @FXML private BorderPane root;
    @FXML private ChatsController chatsViewController;
    @FXML private MessagesController messagesViewController;

    private final Consumer<MessageDelivery> messageListener = delivery -> Platform.runLater(() -> {
        String activeChatUsername = messagesViewController == null ? null : messagesViewController.getCurrentChatUsername();
        if (chatsViewController != null) {
            chatsViewController.applyMessageDelivery(delivery, activeChatUsername);
        }
        if (messagesViewController != null) {
            messagesViewController.handleRealtimeDelivery(delivery);
        }
    });

    private boolean listenerRegistered;
    private MainController mainController;

    @FXML
    public void initialize() {
        chatsViewController.setMessengerController(this);
        messagesViewController.setMessengerController(this);

        // TODO: How does this listeners work?
        root.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null && !listenerRegistered) {
                MyApplication.clientManager.addMessageListener(messageListener);
                listenerRegistered = true;
            } else if (newScene == null && listenerRegistered) {
                MyApplication.clientManager.removeMessageListener(messageListener);
                listenerRegistered = false;
            }
        });
    }

    @Override
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
        mainController.setNavBar(true, "Chats", false);
        mainController.setRefreshButtonVisible(false);
        if (mainController.selectedChatUsername != null && !mainController.selectedChatUsername.isBlank()) {
            openConversation(new Chat(mainController.selectedChatUsername));
        }
    }

    public void openConversation(Chat chat) {
        chatsViewController.markConversationOpen(chat.getName());
        messagesViewController.openConversation(chat);
    }

    // Update chat list after sending a message
    public void onLocalMessage(MessageDelivery delivery) {
        chatsViewController.applyMessageDelivery(delivery, messagesViewController.getCurrentChatUsername());
    }
}
