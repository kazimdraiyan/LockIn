package app.lockin.lockin;

import app.lockin.lockin.client.ClientManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class LockInApplication extends Application {
    public static ClientManager clientManager = new ClientManager();

    @Override
    public void start(Stage stage) throws IOException {
        // TODO: Should I use the separate thread here?
        new Thread(() -> {
            try {
                clientManager.connect("localhost", 1234);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        FXMLLoader fxmlLoader = new FXMLLoader(LockInApplication.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setMaximized(true);
        stage.setTitle("LockIn");
        stage.setScene(scene);
        stage.show();
    }
}
