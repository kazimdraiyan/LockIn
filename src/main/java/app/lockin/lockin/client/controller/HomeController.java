package app.lockin.lockin.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class HomeController implements MainControllerAware {
    /*@FXML
    private AnchorPane rootPane;
    @FXML
    private AnchorPane mainPane;
    @FXML
    private AnchorPane lockinPane;
    @FXML
    private AnchorPane lockinPane2;
    @FXML
    private AnchorPane lockinPane3;*/
    @FXML private Label profileInitialsLabel;
    @FXML private Label sidebarInitialsLabel;
    @FXML private Label sidebarUsernameLabel;
    @FXML private VBox feedContainer;
    @FXML private VBox contactsContainer;

    private MainController mainController;

    @Override
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }
    @FXML
    protected void onProfilePicClick() {
        // TODO: navigate to profile view
        System.out.println("Profile clicked");
    }
    @FXML
    protected void onUploadFileClick() {
        // TODO: open file chooser
        System.out.println("Upload file clicked");
    }

    public void onMessengerButtonClick(ActionEvent actionEvent) throws IOException {
        mainController.navigateTo("messenger-view.fxml");
    }
}
