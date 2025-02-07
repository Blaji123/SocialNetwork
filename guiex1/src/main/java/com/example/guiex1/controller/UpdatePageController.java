package com.example.guiex1.controller;

import com.example.guiex1.domain.User;
import com.example.guiex1.domain.validators.ValidationException;
import com.example.guiex1.services.FriendshipRequestService;
import com.example.guiex1.services.FriendshipService;
import com.example.guiex1.services.MessageService;
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
    @FXML
    private Button backButton;
    @FXML
    private Button deleteButton;

    private UserService userService;
    private User currentUser;
    private FriendshipService friendshipService;
    private MessageService messageService;
    private FriendshipRequestService friendshipRequestService;

    public void setServices(FriendshipService friendshipService, UserService userService, MessageService messageService, FriendshipRequestService friendshipRequestService) {
        this.friendshipService = friendshipService;
        this.userService = userService;
        this.messageService = messageService;
        this.friendshipRequestService = friendshipRequestService;
    }

    public void setUser(User user) {
        this.currentUser = user;
        loadUserData();
        setProfileImage();
    }

    @FXML
    public void initialize() {
//        setBackArrow();

        updateButton.setOnAction(event -> handleUpdate());
        deleteButton.setOnAction(event -> confirmAndDeleteUser());
        backButton.setOnAction(event -> goToFriendsPage());
    }

    private void setProfileImage(){
        Circle clip = new Circle(75, 75, 75);
        profilePhoto.setClip(clip);
        profilePhoto.setImage(new Image(new ByteArrayInputStream(currentUser.getPhoto())));
    }

    @FXML
    private void handleChangeProfilePhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Photo");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(profilePhoto.getScene().getWindow());
        if (selectedFile != null) {
            try {
                byte[] photoBytes = java.nio.file.Files.readAllBytes(selectedFile.toPath());
                Image newPhoto = new Image(selectedFile.toURI().toString());
                profilePhoto.setImage(newPhoto);
                currentUser.setPhoto(photoBytes);
                userService.updateUser(currentUser);
                Circle clip = new Circle(75, 75, 75);
                profilePhoto.setClip(clip);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadUserData() {
        firstNameField.setText(currentUser.getFirstName());
        lastNameField.setText(currentUser.getLastName());
        emailField.setText(currentUser.getEmail());
    }

    private void confirmAndDeleteUser() {
        if (MessageAlert.showConfirmation("Delete Account", "Are you sure you want to delete your account?")) {
            userService.deleteUser(currentUser.getId());
            navigateToLoginPage();
        }
    }

    private void handleUpdate() {
        String updatedFirstName = firstNameField.getText().trim();
        String updatedLastName = lastNameField.getText().trim();
        String updatedEmail = emailField.getText().trim();

        try {
            currentUser.setFirstName(updatedFirstName);
            currentUser.setLastName(updatedLastName);
            currentUser.setEmail(updatedEmail);

            userService.updateUser(currentUser);
            goToFriendsPage();
        } catch (ValidationException ex) {
            MessageAlert.showErrorMessage((Stage) updateButton.getScene().getWindow(), ex.getMessage());
        }
    }

    private void navigateToLoginPage() {
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.close();
    }

    private void goToFriendsPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/guiex1/views/friends-page-view.fxml"));
            Parent friendsPage = loader.load();
            FriendsPageController friendsPageController = loader.getController();
            friendsPageController.setServices(friendshipService, userService, messageService, friendshipRequestService);
            friendsPageController.setUser(currentUser);
            Scene scene = new Scene(friendsPage, 1200, 900);
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}