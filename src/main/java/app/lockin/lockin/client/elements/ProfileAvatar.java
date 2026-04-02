package app.lockin.lockin.client.elements;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;

public class ProfileAvatar extends StackPane {
    private static final double DEFAULT_SIZE = 40;
    private static final double MIN_FONT_SIZE = 11;
    private static final double FONT_RATIO = 0.42;

    private final Label textLabel = new Label();
    private final StringProperty text = new SimpleStringProperty(this, "text", "");
    private final DoubleProperty size = new SimpleDoubleProperty(this, "size", DEFAULT_SIZE);

    public ProfileAvatar() {
        getStyleClass().add("profile-avatar");
        setAlignment(Pos.CENTER);

        textLabel.getStyleClass().add("profile-avatar-text");
        getChildren().add(textLabel);

        text.addListener((obs, oldValue, newValue) -> updateText());
        size.addListener((obs, oldValue, newValue) -> updateSize());

        updateText();
        updateSize();
    }

    public String getText() {
        return text.get();
    }

    public void setText(String value) {
        text.set(value);
    }

    public StringProperty textProperty() {
        return text;
    }

    public double getSize() {
        return size.get();
    }

    public void setSize(double value) {
        size.set(value);
    }

    public DoubleProperty sizeProperty() {
        return size;
    }

    private void updateText() {
        String value = text.get();
        if (value == null || value.isBlank()) {
            textLabel.setText("");
            return;
        }

        textLabel.setText(String.valueOf(value.trim().charAt(0)).toUpperCase());
    }

    private void updateSize() {
        double avatarSize = size.get();
        setMinSize(avatarSize, avatarSize);
        setPrefSize(avatarSize, avatarSize);
        setMaxSize(avatarSize, avatarSize);
        textLabel.setFont(Font.font(Math.max(MIN_FONT_SIZE, avatarSize * FONT_RATIO)));
    }
}
