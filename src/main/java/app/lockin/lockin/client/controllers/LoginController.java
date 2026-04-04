package app.lockin.lockin.client.controllers;

import app.lockin.lockin.client.MyApplication;
import app.lockin.lockin.common.models.Session;
import app.lockin.lockin.common.requests.LoginRequest;
import app.lockin.lockin.common.response.Response;
import app.lockin.lockin.common.response.ResponseStatus;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;

public class LoginController implements MainControllerAware {
    @FXML public TextField usernameField;
    @FXML public PasswordField passwordField;
    @FXML public Button signInButton;

    private MainController mainController;

    @Override
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
        mainController.setNavBar(true, "Login", false);
    }

    @FXML
    protected void onHomeButtonClick() throws IOException {
        mainController.navigateReplacingRoot("welcome-view.fxml");
    }

    @FXML
    protected void onSignUpLinkClick() {
        try { mainController.navigateReplacingCurrent("sign-up-view.fxml"); }
        catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private Label errorLabel;

    private void showError(String message) {
        Platform.runLater(() -> {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
        });
    }

    private void clearError() {
        Platform.runLater(() -> {
            errorLabel.setText("");
            errorLabel.setVisible(false);
        });
    }

    @FXML
    protected void login() {
        String name = usernameField.getText();
        String password = passwordField.getText();

        if (name.isEmpty() || password.isEmpty()) {
            showError("Please fill all the fields");
            return;
        }
        clearError();

        new Thread(() -> {
            try {
                LoginRequest request = new LoginRequest(usernameField.getText(), passwordField.getText());
                Response response = MyApplication.clientManager.sendRequest(request);
                System.out.println(response.getMessage());
                System.out.println(response.getData());

                if (response.getStatus() == ResponseStatus.SUCCESS) {
                    MyApplication.clientManager.isLoggedIn = true;
                    MyApplication.clientManager.username = ((Session) response.getData()).getUsername();
                    MyApplication.saveToken(((Session) response.getData()).getToken());
                    Platform.runLater(() -> {
                        try { mainController.navigateReplacingRoot("home-view.fxml"); }
                        catch (Exception e) { e.printStackTrace(); }
                    });
                } else {
                    showError("Login failed: " + response.getMessage());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
