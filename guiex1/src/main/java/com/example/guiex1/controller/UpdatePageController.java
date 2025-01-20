package com.example.guiex1.controller;

import com.example.guiex1.domain.User;
import com.example.guiex1.domain.validators.ValidationException;
import com.example.guiex1.services.FriendshipService;
import com.example.guiex1.services.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class UpdatePageController {

    @FXML
    private ImageView profilePhoto;

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField emailField;

    @FXML
    private Button updateButton;

    private UserService userService;
    private User currentUser;
    private FriendshipService friendshipService;

    @FXML
    private Button backButton;

    @FXML
    private Button deleteButton;

    public void setFriendshipService(FriendshipService friendshipService) {
        this.friendshipService = friendshipService;
    }

    public void setUser(User user) {
        this.currentUser = user;
        loadUserData();
        setProfileImage();
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @FXML
    public void initialize() {
//        setBackArrow();

        updateButton.setOnAction(event -> handleUpdate());
        deleteButton.setOnAction(event -> confirmAndDeleteUser());
        backButton.setOnAction(event -> goToFriendsPage());
    }

    private void setProfileImage(){
        Circle clip = new Circle(75, 75, 75); // Center x, y, radius
        profilePhoto.setClip(clip);
        // Set a default placeholder image (optional)
        profilePhoto.setImage(new Image(new ByteArrayInputStream(currentUser.getPhoto())));
    }

    @FXML
    private void handleChangeProfilePhoto() {
        // Open a FileChooser for selecting a new photo
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Photo");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(profilePhoto.getScene().getWindow());
        if (selectedFile != null) {
            try {
                // Convert the selected file to a byte array
                byte[] photoBytes = java.nio.file.Files.readAllBytes(selectedFile.toPath());

                // Update the profile photo in the UI
                Image newPhoto = new Image(selectedFile.toURI().toString());
                profilePhoto.setImage(newPhoto);

                // Set the new photo bytes in the currentUser object (adjust this to your user model)
                currentUser.setPhoto(photoBytes);

                // Save the updated user to the database
                userService.updateUser(currentUser);

                // Reapply the circular clipping to the new photo
                Circle clip = new Circle(75, 75, 75);
                profilePhoto.setClip(clip);
            } catch (IOException e) {
                e.printStackTrace(); // Log the error or show a user-friendly message
            }
        }
    }



    private void setBackArrow() {
        try{
            backButton.setPrefSize(50, 50);
            Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/guiex1/images/backArrow.png")));
            ImageView imageView = new ImageView(image);
            imageView.setFitHeight(50);
            imageView.setPreserveRatio(true);
            backButton.setGraphic(imageView);
        }catch (Exception ex){
            System.out.println(ex.getMessage());
        }
    }

    private void loadUserData() {
        firstNameField.setText(currentUser.getFirstName());
        lastNameField.setText(currentUser.getLastName());
        emailField.setText(currentUser.getEmail());
    }

    private void confirmAndDeleteUser() {
        // Show confirmation dialog
        if (MessageAlert.showConfirmation("Delete Account", "Are you sure you want to delete your account?")) {
            userService.deleteUser(currentUser.getId());
            navigateToLoginPage();
        }
    }

    private void navigateToLoginPage() {
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.close();
    }

    private void handleUpdate() {
        String updatedFirstName = firstNameField.getText().trim();
        String updatedLastName = lastNameField.getText().trim();
        String updatedEmail = emailField.getText().trim();

        try {
            currentUser.setFirstName(updatedFirstName);
            currentUser.setLastName(updatedLastName);
            currentUser.setEmail(updatedEmail);

            // Call the service to update user information
            userService.updateUser(currentUser);

            // Return to the friends page (navigation can be optional if this is part of your flow)
            goToFriendsPage();
        } catch (ValidationException ex) {
            // Display error message
            MessageAlert.showErrorMessage((Stage) updateButton.getScene().getWindow(), ex.getMessage());
        }
    }

    private void goToFriendsPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/guiex1/views/friends-page-view.fxml"));
            Parent friendsPage = loader.load();
            FriendsPageController friendsPageController = loader.getController();
            friendsPageController.setUserService(userService);
            friendsPageController.setFriendshipService(friendshipService);
            friendsPageController.setUser(currentUser);
            Scene scene = new Scene(friendsPage, 1200, 900);
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}