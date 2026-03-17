package app.lockin.lockin;

import app.lockin.lockin.client.ClientManager;
import app.lockin.lockin.util.ThemeManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class LockInApplication extends Application {
    public static ClientManager clientManager = new ClientManager();

    private static Path getTokenPath() {
        String home = System.getProperty("user.home"); // Platform independent. For example, in Windows: C:\Users\User\
        return Path.of(home, ".lockin", "token.txt");
    }

    @Override
    public void start(Stage stage) throws IOException {
        clientManager.connect("localhost", 1234);

        FXMLLoader fxmlLoader = new FXMLLoader(LockInApplication.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        ThemeManager.register(scene);

        stage.setMaximized(true);
        stage.setTitle("LockIn");
        stage.setScene(scene);
        stage.show();
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
