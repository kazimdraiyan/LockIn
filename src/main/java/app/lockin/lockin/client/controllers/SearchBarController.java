package app.lockin.lockin.client.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class SearchBarController {
    @FXML
    private TextField inputField;

    public TextField getInputField() {
        return inputField;
    }

    public void setPromptText(String promptText) {
        inputField.setPromptText(promptText);
    }
}
