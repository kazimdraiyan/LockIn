package app.lockin.lockin.client.utils;

import app.lockin.lockin.client.MyApplication;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public final class UiIcons {
    private UiIcons() {
    }

    public static ImageView icon(String baseName, double size) {
        String fileName = ThemeManager.isDarkMode() ? baseName + "_white.png" : baseName + ".png";
        ImageView icon = new ImageView(new Image(MyApplication.getIcon(fileName).toExternalForm()));
        icon.setFitWidth(size);
        icon.setFitHeight(size);
        icon.setPreserveRatio(true);
        return icon;
    }
}
