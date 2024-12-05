package com.example.guiex1.controller;

import com.example.guiex1.domain.User;
import com.example.guiex1.domain.validators.ValidationException;
import com.example.guiex1.services.FriendshipService;
import com.example.guiex1.services.UserService;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;

public class RegisterPageController {

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label passwordNote;

    @FXML
    private Button registerButton;

    @FXML
    private Button loginButton;

    private UserService userService;
    private FriendshipService friendshipService;

    public void setFriendshipService(FriendshipService friendshipService) {
        this.friendshipService = friendshipService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }


    @FXML
    public void initialize() {
        registerButton.setOnAction(event -> handleRegister());
        loginButton.setOnAction(event -> goToLoginPage());

        passwordField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            passwordNote.setVisible(newValue); // Show note when password field is focused
        });
    }

    private void handleRegister() {
        // Get values from the form
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        // Create a new User object (ID will be auto-generated)
        User newUser = new User(firstName, lastName, email, password);
        try{
            userService.addUser(newUser);
            clearFormFields();
            MessageAlert.showMessage((Stage) registerButton.getScene().getWindow(), Alert.AlertType.CONFIRMATION, "Registration Successful", "User Registered Successfully");
        }catch (ValidationException ex){
            MessageAlert.showErrorMessage((Stage) registerButton.getScene().getWindow(), ex.getMessage());
        }
    }

    private void clearFormFields() {
        firstNameField.clear();
        lastNameField.clear();
        emailField.clear();
        passwordField.clear();
    }

    private void goToLoginPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../views/login-page-view.fxml"));
            Parent loginPage = loader.load();
            LoginPageController loginPageController = loader.getController();
            loginPageController.setUserService(userService);
            loginPageController.setFriendshipService(friendshipService);
            Scene scene = new Scene(loginPage, 1200, 900);
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
