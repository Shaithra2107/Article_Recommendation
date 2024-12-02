package com.example.article;

import com.example.article.App.User;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;

public class ManageProfileController {

    @FXML
    private TextField fullNameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneField;
    @FXML
    private RadioButton maleRadioButton;
    @FXML
    private RadioButton femaleRadioButton;
    @FXML
    private RadioButton otherRadioButton;
    @FXML
    private DatePicker dobField;
    @FXML
    private Label messageLabel;
    @FXML
    private ImageView profileImageView;

    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> userCollection;
    private ObjectId userId; // Use ObjectId for querying `_id` field
    private File profilePictureFile;

    @FXML
    public void initialize() {
        // Get the logged-in user ID
        userId = new ObjectId(User.getLoggedInUserId());

        // Set up MongoDB connection
        ConnectionString connectionString = new ConnectionString("mongodb+srv://shaithra20232694:123shaithra@cluster0.cwjpj.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0");
        MongoClientSettings settings = MongoClientSettings.builder().applyConnectionString(connectionString).build();
        ServerApi serverApi = ServerApi.builder()
                .version(ServerApiVersion.V1)
                .build();
        mongoClient = MongoClients.create(settings);
        database = mongoClient.getDatabase("News_Recommendation");
        userCollection = database.getCollection("Users");

        // Load the user's profile details
        loadUserProfile();
    }

    // Load user profile data from MongoDB
    private void loadUserProfile() {
        Document userDoc = userCollection.find(new Document("_id", userId)).first();
        if (userDoc != null) {
            // Populate fields with user data
            fullNameField.setText(userDoc.getString("fullName"));
            emailField.setText(userDoc.getString("email"));
            phoneField.setText(userDoc.getString("phoneNumber"));
            dobField.setValue(java.time.LocalDate.parse(userDoc.getString("dateOfBirth")));

            String gender = userDoc.getString("gender");
            if ("Male".equals(gender)) {
                maleRadioButton.setSelected(true);
            } else if ("Female".equals(gender)) {
                femaleRadioButton.setSelected(true);
            } else {
                otherRadioButton.setSelected(true);
            }

            // Load the profile picture
            String profilePicturePath = userDoc.getString("profilePicturePath");
            if (profilePicturePath != null && Files.exists(new File(profilePicturePath).toPath())) {
                profileImageView.setImage(new Image(new File(profilePicturePath).toURI().toString()));
                profilePictureFile = new File(profilePicturePath);
            }
        } else {
            showAlert("Error: User data not found.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleSaveChanges(ActionEvent actionEvent) {
        String fullName = fullNameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();
        String gender = maleRadioButton.isSelected() ? "Male" :
                femaleRadioButton.isSelected() ? "Female" : "Other";
        String dob = dobField.getValue() != null ? dobField.getValue().toString() : "";

        if (fullName.isEmpty() || email.isEmpty() || phone.isEmpty() || gender.isEmpty() || dob.isEmpty()) {
            showAlert("Please fill in all fields.", Alert.AlertType.WARNING);
            return;
        }

        Document updatedUser = new Document("fullName", fullName)
                .append("email", email)
                .append("phoneNumber", phone)
                .append("gender", gender)
                .append("dateOfBirth", dob);

        if (profilePictureFile != null) {
            updatedUser.append("profilePicturePath", profilePictureFile.getAbsolutePath());
        }

        try {
            // Update the user profile in the database
            userCollection.updateOne(new Document("_id", userId), new Document("$set", updatedUser));
            showAlert("Profile updated successfully!", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error updating profile: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleChangeProfilePicture(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.png", "*.jpeg"));
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            profilePictureFile = selectedFile;
            profileImageView.setImage(new Image(selectedFile.toURI().toString()));
        }
    }

    @FXML
    private void handleBack(ActionEvent actionEvent) {
        try {
            Parent dashboardRoot = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("Dashboard.fxml")));
            Scene dashboardScene = new Scene(dashboardRoot);
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(dashboardScene);
            stage.setTitle("Dashboard");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error loading Dashboard.", Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setContentText(message);
        alert.show();
    }

    public void handleCancel(ActionEvent actionEvent) {

    }
}
