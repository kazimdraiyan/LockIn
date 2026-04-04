package app.lockin.lockin.client.controllers;

import app.lockin.lockin.client.MyApplication;
import app.lockin.lockin.common.models.Session;
import app.lockin.lockin.common.requests.SignUpRequest;
import app.lockin.lockin.common.response.Response;
import app.lockin.lockin.common.response.ResponseStatus;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;

public class SignUpController implements MainControllerAware {
    @FXML
    public TextField usernameField;
    @FXML
    public PasswordField passwordField;
    @FXML
    public PasswordField confirmPasswordField;
    @FXML
    public Button signUpButton;

    private MainController mainController;

    @Override
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
        mainController.setNavBar(true, "Create an Account", false);
    }

    @FXML
    protected void onHomeButtonClick() throws IOException {
        mainController.navigateReplacingRoot("welcome-view.fxml");
    }

    @FXML
    protected void onSignInLinkClick() {
        try {
            mainController.navigateReplacingCurrent("login-view.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
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
    public void signUp() {
        String name = usernameField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (name.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showError("Please fill all the fields");
            return;
        }
        clearError();

        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match");
            return;
        }
        clearError();

        // Networking calls should be done in a separate thread so that JavaFX UI is not interrupted
        // TODO: Should I do the threading here?
        new Thread(() -> {
            try {
                SignUpRequest request = new SignUpRequest(usernameField.getText(), passwordField.getText());

                // TODO: Should I rename LockInApplication?
                MyApplication.clientManager.send(request);

                Response response = MyApplication.clientManager.receive();
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
                    // TODO: Show error on GUI using Platform.runLater as well
                    System.out.println("Sign up failed: " + response.getMessage());
                }

                // TODO: Learn more about updating UI safely
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
