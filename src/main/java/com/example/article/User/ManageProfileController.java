package com.example.article.User;

// Import necessary libraries and classes
import com.example.article.App.User; // User class to get logged-in user ID
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

    // FXML elements linked to the UI
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

    // MongoDB connection variables
    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> userCollection;
    private ObjectId userId; // User ID stored as ObjectId for MongoDB `_id` field
    private File profilePictureFile; // File reference for the profile picture

    @FXML
    public void initialize() {
        // Get the ID of the logged-in user from the User class
        userId = new ObjectId(User.getLoggedInUserId());

        // Set up MongoDB connection with connection string and settings
        ConnectionString connectionString = new ConnectionString("mongodb://localhost:27017");
        MongoClientSettings settings = MongoClientSettings.builder().applyConnectionString(connectionString).build();
        ServerApi serverApi = ServerApi.builder()
                .version(ServerApiVersion.V1)
                .build();
        mongoClient = MongoClients.create(settings);

        // Access the "News_Recommendation" database and "Users" collection
        database = mongoClient.getDatabase("News_Recommendation");
        userCollection = database.getCollection("Users");

        // Load the profile details of the user from the database
        loadUserProfile();
    }

    // Method to load user profile data from MongoDB
    private void loadUserProfile() {
        // Find the document in MongoDB corresponding to the user's ID
        Document userDoc = userCollection.find(new Document("_id", userId)).first();

        if (userDoc != null) {
            // Populate the UI fields with data from the document
            fullNameField.setText(userDoc.getString("fullName"));
            emailField.setText(userDoc.getString("email"));
            phoneField.setText(userDoc.getString("phoneNumber"));
            dobField.setValue(java.time.LocalDate.parse(userDoc.getString("dateOfBirth")));

            // Set the appropriate radio button for the user's gender
            String gender = userDoc.getString("gender");
            if ("Male".equals(gender)) {
                maleRadioButton.setSelected(true);
            } else if ("Female".equals(gender)) {
                femaleRadioButton.setSelected(true);
            } else {
                otherRadioButton.setSelected(true);
            }

            // Load the profile picture if the file path exists
            String profilePicturePath = userDoc.getString("profilePicturePath");
            if (profilePicturePath != null && Files.exists(new File(profilePicturePath).toPath())) {
                profileImageView.setImage(new Image(new File(profilePicturePath).toURI().toString()));
                profilePictureFile = new File(profilePicturePath);
            }
        } else {
            // Show an error alert if the user data is not found
            showAlert("Error: User data not found.", Alert.AlertType.ERROR);
        }
    }

    // Event handler for saving changes to the user profile
    @FXML
    private void handleSaveChanges(ActionEvent actionEvent) {
        // Get input from the UI fields
        String fullName = fullNameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();
        String gender = maleRadioButton.isSelected() ? "Male" :
                femaleRadioButton.isSelected() ? "Female" : "Other";
        String dob = dobField.getValue() != null ? dobField.getValue().toString() : "";

        // Validate input fields
        if (fullName.isEmpty() || email.isEmpty() || phone.isEmpty() || gender.isEmpty() || dob.isEmpty()) {
            showAlert("Please fill in all fields.", Alert.AlertType.WARNING);
            return;
        }

        // Create a MongoDB document with the updated user data
        Document updatedUser = new Document("fullName", fullName)
                .append("email", email)
                .append("phoneNumber", phone)
                .append("gender", gender)
                .append("dateOfBirth", dob);

        // Include the profile picture path if a new file is selected
        if (profilePictureFile != null) {
            updatedUser.append("profilePicturePath", profilePictureFile.getAbsolutePath());
        }

        try {
            // Update the user's document in the database
            userCollection.updateOne(new Document("_id", userId), new Document("$set", updatedUser));
            showAlert("Profile updated successfully!", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error updating profile: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // Event handler for changing the profile picture
    @FXML
    private void handleChangeProfilePicture(ActionEvent actionEvent) {
        // Open a file chooser dialog to select a new profile picture
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.png", "*.jpeg"));
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            // Update the profile picture and the file reference
            profilePictureFile = selectedFile;
            profileImageView.setImage(new Image(selectedFile.toURI().toString()));
        }
    }

    // Event handler for navigating back to the Dashboard
    @FXML
    private void handleBack(ActionEvent actionEvent) {
        try {
            // Load the Dashboard scene
            Parent dashboardRoot = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/example/article/Dashboard.fxml")));
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

    // Utility method to display alerts
    private void showAlert(String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setContentText(message);
        alert.show();
    }

    // Event handler for canceling changes (currently empty, can be implemented as needed)
    public void handleCancel(ActionEvent actionEvent) {

    }
}
