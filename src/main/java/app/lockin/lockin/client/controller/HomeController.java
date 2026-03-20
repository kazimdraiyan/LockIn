package app.lockin.lockin.client.controller;

import javafx.event.ActionEvent;
import app.lockin.lockin.LockInApplication;
import app.lockin.lockin.server.model.Chat;
import app.lockin.lockin.server.request.FetchRequest;
import app.lockin.lockin.server.request.FetchType;
import app.lockin.lockin.server.response.Response;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.io.IOException;

import java.io.IOException;
import java.util.ArrayList;

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
