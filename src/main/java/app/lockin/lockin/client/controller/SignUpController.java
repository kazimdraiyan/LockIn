package app.lockin.lockin.client.controller;

import app.lockin.lockin.LockInApplication;
import app.lockin.lockin.server.request.LoginRequest;
import app.lockin.lockin.server.request.SignUpRequest;
import app.lockin.lockin.server.response.Response;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class SignUpController {
    @FXML
    public TextField nameField;

    @FXML
    public TextField emailField;

    @FXML
    public PasswordField passwordField;

    @FXML
    public PasswordField confirmPasswordField;

    @FXML
    public Button signUpButton;

    @FXML
    public void signUp() {
        String name = nameField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // TODO: Show error message on the GUI
        if (name.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            System.out.println("Please fill all the fields");
            return;
        }
        if (!password.equals(confirmPassword)) {
            System.out.println("Passwords do not match");
            return;
        }

        // Networking calls should be done in a separate thread so that JavaFX UI is not interrupted
        // TODO: Should I do the threading here?
        new Thread(() -> {
            try {
                SignUpRequest request =
                        new SignUpRequest(nameField.getText(), emailField.getText(),
                                passwordField.getText());

                // TODO: Should I rename LockInApplication?
                LockInApplication.clientManager.send(request);

                Response response =
                        LockInApplication.clientManager.receive();
                System.out.println(response.toString());

                // TODO: Learn more about updating UI safely
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}