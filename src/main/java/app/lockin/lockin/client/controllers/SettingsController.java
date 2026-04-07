package app.lockin.lockin.client.controllers;

import app.lockin.lockin.client.MyApplication;
import app.lockin.lockin.common.requests.ChangePasswordRequest;
import app.lockin.lockin.common.requests.LogoutRequest;
import app.lockin.lockin.common.response.Response;
import app.lockin.lockin.common.response.ResponseStatus;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;

import java.io.IOException;

public class SettingsController implements MainControllerAware {
    @FXML private PasswordField oldPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button changePasswordButton;
    @FXML private Button logoutButton;
    @FXML private Label statusLabel;

    private MainController mainController;

    @Override
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
        mainController.setNavBar(true, "Settings", true);
        mainController.setRefreshButtonVisible(false);
    }

    @FXML
    protected void onChangePasswordClick() {
        String oldPassword = oldPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (oldPassword.isBlank()) {
            statusLabel.setText("Please enter your current password.");
            return;
        }

        if (newPassword.isBlank()) {
            statusLabel.setText("Please enter a new password.");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            statusLabel.setText("New passwords do not match.");
            return;
        }

        changePasswordButton.setDisable(true);
        statusLabel.setText("Changing password...");
        new Thread(() -> {
            try {
                ChangePasswordRequest request = new ChangePasswordRequest(oldPassword, newPassword);
                Response response = sendRequest(request);

                if (response != null && response.getStatus() == ResponseStatus.SUCCESS) {
                    Platform.runLater(() -> {
                        oldPasswordField.clear();
                        newPasswordField.clear();
                        confirmPasswordField.clear();
                        statusLabel.setText("Password changed successfully.");
                        changePasswordButton.setDisable(false);
                    });
                } else {
                    String message = response == null ? "No response from server." : response.getMessage();
                    Platform.runLater(() -> {
                        statusLabel.setText(message);
                        changePasswordButton.setDisable(false);
                    });
                }
            } catch (IOException e) {
                Platform.runLater(() -> {
                    statusLabel.setText(e.getMessage());
                    changePasswordButton.setDisable(false);
                });
            }
        }).start();
    }

    @FXML
    protected void onLogoutClick() {
        logoutButton.setDisable(true);
        statusLabel.setText("Logging out...");
        new Thread(() -> {
            try {
                LogoutRequest request = new LogoutRequest();
                sendRequest(request);
            } catch (Exception e) {
                System.out.println("Server logout failed, continuing with local logout: " + e.getMessage());
            }
            Platform.runLater(this::performLocalLogout);
        }).start();
    }

    private void performLocalLogout() {
        MyApplication.clientManager.clearAuthenticatedSession();
        MyApplication.deleteToken();
        try {
            mainController.navigateReplacingRoot("welcome-view.fxml");
        } catch (Exception e) {
            statusLabel.setText("Could not open welcome page.");
            logoutButton.setDisable(false);
        }
    }

    private Response sendRequest(app.lockin.lockin.common.requests.Request request) throws IOException {
        synchronized (MyApplication.clientManager) {
            return MyApplication.clientManager.sendRequest(request);
        }
    }
}
