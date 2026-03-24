package app.lockin.lockin.client.model;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public class Page {
    public Parent root;
    public FXMLLoader fxmlLoader;

    public Page(Parent root, FXMLLoader fxmlLoader) {
        this.root = root;
        this.fxmlLoader = fxmlLoader;
    }
}
