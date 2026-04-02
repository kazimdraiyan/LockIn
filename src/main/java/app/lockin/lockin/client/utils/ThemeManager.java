package app.lockin.lockin.client.utils;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

public class ThemeManager {
    private static final String BASE_CSS =
            ThemeManager.class.getResource("/app/lockin/lockin/css/style.css").toExternalForm();

    private static final String DARK_CSS =
            ThemeManager.class.getResource("/app/lockin/lockin/css/dark-theme.css").toExternalForm();

    private static final String LIGHT_CSS =
            ThemeManager.class.getResource("/app/lockin/lockin/css/light-theme.css").toExternalForm();

    private static final Preferences prefs =
            Preferences.userNodeForPackage(ThemeManager.class);

    // Track all active scenes so switching affects all of them
    private static final List<Scene> registeredScenes = new ArrayList<>();
    private static boolean darkMode = prefs.getBoolean("darkMode", false);

    static{
        System.out.println("Loaded darkMode from prefs: " + darkMode);
    }

    /** Register a scene so it participates in theme switching */
    public static void register(Scene scene) {
        if (!scene.getStylesheets().contains(BASE_CSS)) {
            scene.getStylesheets().add(BASE_CSS);
        }
        registeredScenes.add(scene);
        applyCurrentTheme(scene);
    }

    /** Toggle between dark and light mode across all registered scenes */
    public static void toggle() {
        darkMode = !darkMode;
        prefs.putBoolean("darkMode", darkMode);
        try{
            prefs.flush();
        }   catch (Exception e){
            e.printStackTrace();
        }
        for (Scene scene : registeredScenes) {
            applyCurrentTheme(scene);
        }
    }

    /** Force set a specific mode */
    /*public static void setDarkMode(boolean enabled) {
        darkMode = enabled;
        for (Scene scene : registeredScenes) {
            if (darkMode) applyDark(scene);
            else applyLight(scene);
        }
    }*/

    public static boolean isDarkMode() { return darkMode; }

    public static void refresh(Scene scene) {
        applyCurrentTheme(scene);
    }

    // Helper functions
    private static void applyCurrentTheme(Scene scene) {
        if (darkMode) applyDark(scene);
        else applyLight(scene);
        applyIcons(scene);
    }

    private static void applyDark(Scene scene) {
        scene.getStylesheets().remove(LIGHT_CSS);
        if (!scene.getStylesheets().contains(BASE_CSS))
            scene.getStylesheets().add(BASE_CSS);
        if (!scene.getStylesheets().contains(DARK_CSS))
            scene.getStylesheets().add(DARK_CSS);
    }

    private static void applyLight(Scene scene) {
        scene.getStylesheets().remove(DARK_CSS);
        if (!scene.getStylesheets().contains(BASE_CSS))
            scene.getStylesheets().add(BASE_CSS);
        if (!scene.getStylesheets().contains(LIGHT_CSS))
            scene.getStylesheets().add(LIGHT_CSS);
    }

    private static void applyIcons(Scene scene) {
        if (scene.getRoot() == null) {
            return;
        }
        applyIcons(scene.getRoot());
    }

    private static void applyIcons(Parent parent) {
        for (Node node : parent.getChildrenUnmodifiable()) {
            if (node instanceof ImageView imageView) {
                applyIcon(imageView);
            }
            if (node instanceof Parent childParent) {
                applyIcons(childParent);
            }
        }
    }

    private static void applyIcon(ImageView imageView) {
        Object storedPath = imageView.getProperties().get("themeIconBasePath");
        String basePath;

        if (storedPath instanceof String path) {
            basePath = path;
        } else {
            Image image = imageView.getImage();
            if (image == null || image.getUrl() == null) {
                return;
            }

            basePath = extractBaseIconPath(image.getUrl());
            if (basePath == null) {
                return;
            }
            imageView.getProperties().put("themeIconBasePath", basePath);
        }

        String themedPath = themedIconPath(basePath);
        if (themedPath == null) {
            return;
        }

        URL resource = ThemeManager.class.getResource(themedPath);
        if (resource != null) {
            imageView.setImage(new Image(resource.toExternalForm()));
        }
    }

    private static String extractBaseIconPath(String imageUrl) {
        String marker = "/app/lockin/lockin/icon/";
        int markerIndex = imageUrl.indexOf(marker);
        if (markerIndex < 0) {
            return null;
        }

        String resourcePath = imageUrl.substring(markerIndex);
        if (resourcePath.endsWith("dark_mode.png") || resourcePath.endsWith("light_mode.png")) {
            return null;
        }
        if (resourcePath.endsWith("-white.png")) {
            return resourcePath.replace("-white.png", ".png");
        }
        return resourcePath;
    }

    private static String themedIconPath(String basePath) {
        if (!darkMode) {
            return basePath;
        }

        if (!basePath.endsWith(".png")) {
            return basePath;
        }

        String whitePath = basePath.replace(".png", "-white.png");
        return ThemeManager.class.getResource(whitePath) != null ? whitePath : basePath;
    }
}
