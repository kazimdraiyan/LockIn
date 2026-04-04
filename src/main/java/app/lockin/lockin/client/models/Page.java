package app.lockin.lockin.client.models;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public class Page {
    public Parent root;
    public FXMLLoader fxmlLoader; // For accessing controller of the current page

    public Page(Parent root, FXMLLoader fxmlLoader) {
        this.root = root;
        this.fxmlLoader = fxmlLoader;
    }
}
