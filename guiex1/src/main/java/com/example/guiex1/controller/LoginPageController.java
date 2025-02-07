package com.example.guiex1.controller;

import com.example.guiex1.domain.User;
import com.example.guiex1.services.FriendshipRequestService;
import com.example.guiex1.services.FriendshipService;
import com.example.guiex1.services.MessageService;
import com.example.guiex1.services.UserService;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;
import java.net.URL;

public class LoginPageController {
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;
    @FXML
    private Button registerButton;

    private UserService userService;
    private FriendshipService friendshipService;
    private MessageService messageService;
    private FriendshipRequestService friendshipRequestService;

    public void setServices(FriendshipService friendshipService, UserService userService, MessageService messageService, FriendshipRequestService friendshipRequestService) {
        this.friendshipService = friendshipService;
        this.userService = userService;
        this.messageService = messageService;
        this.friendshipRequestService = friendshipRequestService;
    }

    @FXML
    public void initialize() {
        loginButton.setOnAction(event -> handleLogin());
        registerButton.setOnAction(event -> openRegisterPage());
    }

    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();
        User user = userService.authenticateUser(email, password);
        if (user != null) {
            openFriendsPage(user);
        } else {
            MessageAlert.showErrorMessage((Stage) loginButton.getScene().getWindow(), "Invalid login credentials. Please try again.");
        }
    }

    private void openFriendsPage(User user) {
        try {
            URL resource = getClass().getResource("/com/example/guiex1/views/friends-page-view.fxml");
            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();
            FriendsPageController controller = loader.getController();
            controller.setServices(friendshipService, userService, messageService, friendshipRequestService);
            controller.setUser(user);
            Stage stage = new Stage();
            stage.setTitle("Friends Page - " + user.getFirstName() + " " + user.getLastName());
            stage.setScene(new Scene(root, 1200, 900));
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void openRegisterPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/guiex1/views/register-page-view.fxml"));
            Parent registerPage = loader.load();
            RegisterPageController registerPageController = loader.getController();
            registerPageController.setServices(friendshipService, userService, messageService, friendshipRequestService);
            Scene scene = new Scene(registerPage, 1200, 900);
            Stage stage = (Stage) registerButton.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}