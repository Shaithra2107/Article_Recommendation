package com.example.article;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;

public class ManageProfileController {
    @FXML
    public ImageView profileImageView;
    @FXML
    public Button changeProfilePictureButton;
    @FXML
    public TextField fullNameField;
    @FXML
    public TextField emailField;
    @FXML
    public TextField phoneField;
    @FXML
    public RadioButton maleRadioButton;
    @FXML
    public RadioButton femaleRadioButton;
    @FXML
    public RadioButton otherRadioButton;
    @FXML
    public DatePicker dobField;
    @FXML
    public PasswordField newPasswordField;
    @FXML
    public Button saveButton;
    @FXML
    public Button cancelButton;

    public void handleSaveChanges(ActionEvent actionEvent) {
    }

    public void handleCancel(ActionEvent actionEvent) {
    }
}
