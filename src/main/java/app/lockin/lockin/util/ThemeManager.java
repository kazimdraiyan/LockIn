package app.lockin.lockin.util;

import javafx.scene.Scene;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

public class ThemeManager {

    private static final String DARK_CSS =
            ThemeManager.class.getResource("/css/dark-theme.css").toExternalForm();

    private static final String LIGHT_CSS =
            ThemeManager.class.getResource("/css/light-theme.css").toExternalForm();

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
        registeredScenes.add(scene);
        if (darkMode) applyDark(scene);
        else applyLight(scene);
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
            if (darkMode) applyDark(scene);
            else applyLight(scene);
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

    // ── private helpers ──────────────────────────────────────────────

    private static void applyDark(Scene scene) {
        scene.getStylesheets().remove(LIGHT_CSS);
        if (!scene.getStylesheets().contains(DARK_CSS))
            scene.getStylesheets().add(DARK_CSS);
    }

    private static void applyLight(Scene scene) {
        scene.getStylesheets().remove(DARK_CSS);
        if (!scene.getStylesheets().contains(LIGHT_CSS))
            scene.getStylesheets().add(LIGHT_CSS);
    }
}
