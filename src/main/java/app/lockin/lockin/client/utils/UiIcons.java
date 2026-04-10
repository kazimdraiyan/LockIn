package app.lockin.lockin.client.utils;

import app.lockin.lockin.client.MyApplication;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public final class UiIcons {
    private static final String ICON_BASE_NAME_KEY = "lockin.icon.baseName";
    private static final String ICON_SIZE_KEY = "lockin.icon.size";
    private static final String ICON_FORCE_WHITE_KEY = "lockin.icon.forceWhite";

    private UiIcons() {
    }

    public static ImageView icon(String baseName, double size) {
        return icon(baseName, size, false);
    }

    public static ImageView iconWhite(String baseName, double size) {
        return icon(baseName, size, true);
    }

    private static ImageView icon(String baseName, double size, boolean forceWhite) {
        String fileName = forceWhite || ThemeManager.isDarkMode() ? baseName + "_white.png" : baseName + ".png";
        ImageView icon = new ImageView(new Image(MyApplication.getIcon(fileName).toExternalForm()));
        icon.setFitWidth(size);
        icon.setFitHeight(size);
        icon.setPreserveRatio(true);
        return icon;
    }

    public static void setButtonIcon(Button button, String baseName, double size) {
        setButtonIcon(button, baseName, size, false);
    }

    public static void setButtonIconWhite(Button button, String baseName, double size) {
        setButtonIcon(button, baseName, size, true);
    }

    private static void setButtonIcon(Button button, String baseName, double size, boolean forceWhite) {
        if (button == null || baseName == null || baseName.isBlank()) {
            return;
        }
        button.getProperties().put(ICON_BASE_NAME_KEY, baseName);
        button.getProperties().put(ICON_SIZE_KEY, size);
        button.getProperties().put(ICON_FORCE_WHITE_KEY, forceWhite);
        button.setGraphic(icon(baseName, size, forceWhite));
    }

    public static void refreshIcons(Node root) {
        if (root == null) {
            return;
        }
        if (root instanceof Button button) {
            Object baseNameValue = button.getProperties().get(ICON_BASE_NAME_KEY);
            Object sizeValue = button.getProperties().get(ICON_SIZE_KEY);
            Object forceWhiteValue = button.getProperties().get(ICON_FORCE_WHITE_KEY);
            if (baseNameValue instanceof String baseName && sizeValue instanceof Number number) {
                boolean forceWhite = forceWhiteValue instanceof Boolean value && value;
                button.setGraphic(icon(baseName, number.doubleValue(), forceWhite));
            }
        }
        if (root instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                refreshIcons(child);
            }
        }
    }
}
