package app.lockin.lockin.client.controllers;

import app.lockin.lockin.client.MyApplication;
import app.lockin.lockin.common.requests.LogoutRequest;
import app.lockin.lockin.common.response.Response;
import app.lockin.lockin.common.response.ResponseStatus;
import javafx.application.Platform;
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
        new Thread(() -> {
            try {
                LogoutRequest request = new LogoutRequest();
                MyApplication.clientManager.send(request);

                Response response = MyApplication.clientManager.receive();
                System.out.println(response.getMessage());

                if (response.getStatus() == ResponseStatus.SUCCESS) {
                    MyApplication.clientManager.isLoggedIn = false;
                    MyApplication.deleteToken();
                    // TODO: Learn more about Platform.runLater
                    Platform.runLater(() -> {
                        try {
                            mainController.navigateReplacingRoot("welcome-view.fxml");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        // TODO: Implement logout
    }
}
