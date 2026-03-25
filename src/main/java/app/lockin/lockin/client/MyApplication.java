package app.lockin.lockin.client;

import app.lockin.lockin.client.utils.ThemeManager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Scene;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.net.URL;

public class MyApplication extends Application {
    public static final ClientManager clientManager = new ClientManager();

    @Override
    public void start(Stage stage) throws IOException {
        clientManager.connect("localhost", 1234);

        FXMLLoader fxmlLoader = new FXMLLoader(getFXML("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        ThemeManager.register(scene);

        stage.setTitle("LockIn");
        stage.setMaximized(true);
        stage.setScene(scene);
        stage.show();
    }

    // TODO: Move these methods to util?
    public static URL getFXML(String fileName) {
        return MyApplication.class.getResource("/app/lockin/lockin/fxml/" + fileName);
    }

    public static URL getIcon(String fileName) {
        return MyApplication.class.getResource("/app/lockin/lockin/icon/" + fileName);
    }

    private static Path getTokenPath() {
        String home = System.getProperty("user.home"); // Platform independent. For example, in Windows: C:\Users\User\
        return Path.of(home, ".lockin", "token.txt");
    }

    public static void saveToken(String token) {
        if (token == null) {
            return;
        }
        Path tokenPath = getTokenPath();
        try {
            Files.createDirectories(tokenPath.getParent()); // Creates the directory if it does not exist
            Files.writeString(tokenPath, token);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error saving token");
        }
    }

    public static void deleteToken() {
        try {
            Files.deleteIfExists(getTokenPath());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error deleting token");
        }
    }

    public static String loadToken() {
        Path tokenPath = getTokenPath();
        try {
            if (!Files.exists(tokenPath)) {
                return null;
            }
            return Files.readString(tokenPath).trim();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error loading token");
            return null;
        }
    }
}
