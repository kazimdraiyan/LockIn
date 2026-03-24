package app.lockin.lockin.client.controller;

import app.lockin.lockin.MyApplication;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
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
        mainController.setNavBar(true, "LockIn", true);
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

    public void onChatsButtonClick(MouseEvent mouseEvent) throws IOException {
        mainController.navigatePush("messenger-view.fxml");
    }

    public void logout(MouseEvent mouseEvent) {
        System.out.println("Logout button clicked");
        // TODO: Implement logout
    }
}
