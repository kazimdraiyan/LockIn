package app.lockin.lockin.client.model;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;
import javafx.geometry.Pos;
import app.lockin.lockin.client.model.Chat;

public class ChatCell extends ListCell<Chat> {
    // Avatar
    private final StackPane avatarPane = new StackPane();
    private final Circle avatarCircle = new Circle(22);
    private final Label avatarLetter = new Label();

    // Text
    private final Label nameLabel = new Label();
    private final Label msgLabel = new Label();
    private final VBox textContainer = new VBox(3);

    // Right side (time + badge)
    private final Label timeLabel = new Label();
    private final Label badgeLabel = new Label();
    private final VBox rightContainer = new VBox(5);

    // Root
    private final HBox root = new HBox(12);

    public ChatCell() {
        // Avatar letter style
        avatarLetter.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
        avatarPane.getChildren().addAll(avatarCircle, avatarLetter);

        // Name + message
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        nameLabel.getStyleClass().add("chat-name");

        msgLabel.setStyle("-fx-font-size: 12px;");
        msgLabel.getStyleClass().add("chat-msg");

        textContainer.getChildren().addAll(nameLabel, msgLabel);
        HBox.setHgrow(textContainer, Priority.ALWAYS);
        textContainer.setAlignment(Pos.CENTER_LEFT);

        // Time label
        timeLabel.setStyle("-fx-font-size: 11px;");
        timeLabel.getStyleClass().add("chat-time");

        // Badge
        badgeLabel.setMinSize(20, 20);
        badgeLabel.setAlignment(Pos.CENTER);
        badgeLabel.setStyle(
                "-fx-background-color: #3B82F6;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 11px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 10px;" +
                        "-fx-padding: 2 5 2 5;"
        );

        rightContainer.setAlignment(Pos.TOP_RIGHT);
        rightContainer.getChildren().addAll(timeLabel, badgeLabel);

        root.setAlignment(Pos.CENTER_LEFT);
        root.setPadding(new Insets(8, 12, 8, 12));
        root.getChildren().addAll(avatarPane, textContainer, rightContainer);
    }

    @Override
    protected void updateItem(Chat item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setGraphic(null);
            setText(null);
        } else {
            // Avatar
            avatarCircle.setFill(item.getAvatarColor());
            avatarLetter.setText(String.valueOf(item.getUserName().charAt(0)).toUpperCase());

            // Text
            nameLabel.setText(item.getUserName());
            msgLabel.setText(item.getLastMessage());
            timeLabel.setText(item.getTimestamp());

            // Badge — only show if unread > 0
            if (item.getUnreadCount() > 0) {
                badgeLabel.setText(String.valueOf(item.getUnreadCount()));
                badgeLabel.setVisible(true);
                badgeLabel.setManaged(true);
            } else {
                badgeLabel.setVisible(false);
                badgeLabel.setManaged(false);
            }

            setGraphic(root);
        }
    }
}
