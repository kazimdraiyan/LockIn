package app.lockin.lockin.client.elements;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;

public class ProfileAvatar extends StackPane {
    private static final double DEFAULT_SIZE = 40;
    private static final double MIN_FONT_SIZE = 11;
    private static final double FONT_RATIO = 0.42;

    private final Label textLabel = new Label();
    private final ImageView imageView = new ImageView();
    private final StringProperty text = new SimpleStringProperty(this, "text", "");
    private final DoubleProperty size = new SimpleDoubleProperty(this, "size", DEFAULT_SIZE);
    private final ObjectProperty<Image> image = new SimpleObjectProperty<>(this, "image");

    public ProfileAvatar() {
        getStyleClass().add("profile-avatar");
        setAlignment(Pos.CENTER);

        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setManaged(false);
        imageView.setVisible(false);

        textLabel.getStyleClass().add("profile-avatar-text");
        getChildren().addAll(imageView, textLabel);

        text.addListener((obs, oldValue, newValue) -> updateText());
        size.addListener((obs, oldValue, newValue) -> updateSize());
        image.addListener((obs, oldValue, newValue) -> updateImage());

        updateText();
        updateSize();
        updateImage();
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

    public Image getImage() {
        return image.get();
    }

    public void setImage(Image value) {
        image.set(value);
    }

    public ObjectProperty<Image> imageProperty() {
        return image;
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
        applyCoverCrop();
    }

    private void updateImage() {
        Image currentImage = image.get();
        boolean hasImage = currentImage != null;
        imageView.setImage(currentImage);
        imageView.setManaged(hasImage);
        imageView.setVisible(hasImage);
        textLabel.setManaged(!hasImage);
        textLabel.setVisible(!hasImage);
        if (hasImage) {
            requestCoverCropWhenReady(currentImage);
        }
    }

    private void requestCoverCropWhenReady(Image img) {
        if (img == null || img.isError()) {
            return;
        }
        if (img.getWidth() > 0 && img.getHeight() > 0) {
            applyCoverCrop();
            return;
        }
        final InvalidationListener[] holder = new InvalidationListener[1];
        holder[0] = observable -> {
            if (img.getWidth() > 0 && img.getHeight() > 0) {
                img.progressProperty().removeListener(holder[0]);
                if (image.get() == img) {
                    Platform.runLater(this::applyCoverCrop);
                }
            }
        };
        img.progressProperty().addListener(holder[0]);
    }

    private void applyCoverCrop() {
        Image img = image.get();
        if (img == null || img.isError()) {
            return;
        }
        double iw = img.getWidth();
        double ih = img.getHeight();
        if (iw <= 0 || ih <= 0) {
            return;
        }
        double avatarSize = size.get();
        double scale = Math.max(avatarSize / iw, avatarSize / ih);
        double fw = iw * scale;
        double fh = ih * scale;
        imageView.setFitWidth(fw);
        imageView.setFitHeight(fh);
        imageView.setClip(new Circle(fw / 2, fh / 2, avatarSize / 2));
    }
}
