package com.example.guiex1.controller;

import com.example.guiex1.domain.Message;
import com.example.guiex1.domain.User;
import com.example.guiex1.services.FriendshipService;
import com.example.guiex1.services.UserService;
import com.example.guiex1.utils.events.UserEntityChangeEvent;
import com.example.guiex1.utils.observer.Observer;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class MessagePageController implements Observer<UserEntityChangeEvent> {

    @FXML
    private ListView<User> friendsListView;

    @FXML
    private ListView<Message> messagesListView;

    @FXML
    private TextField messageField;

    @FXML
    private Button sendButton;

    @FXML
    private Button backButton;

    private UserService userService;
    private FriendshipService friendshipService;
    private User currentUser;
    private User selectedFriend;

    public void setFriendshipService(FriendshipService friendshipService) {
        this.friendshipService = friendshipService;
    }

    public void setUserService(UserService userService) {

        this.userService = userService;
        userService.addObserver(this);
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadFriendsList();
    }

    @FXML
    private void initialize() {
        friendsListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                selectedFriend = newValue;
                loadMessages();
            }
        });

        setBackArrow();

        sendButton.setOnAction(event -> sendMessage());
        backButton.setOnAction(event -> goToFriendsPage());
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

    private void loadFriendsList() {
        List<User> friends = friendshipService.getFriendsForUser(currentUser);
        friendsListView.getItems().addAll(friends);
    }

    private void loadMessages() {
        messagesListView.getItems().clear();
        List<Message> messages = userService.getMessages(currentUser.getId(), selectedFriend.getId());
        messagesListView.getItems().addAll(messages);
    }

    private void sendMessage() {
        String content = messageField.getText().trim();
        if (!content.isEmpty() && selectedFriend != null) {
            userService.addMessage(currentUser.getId(), selectedFriend.getId(), content);
            messageField.clear();
            loadMessages();
        }
    }

    @Override
    public void update(UserEntityChangeEvent userEntityChangeEvent) {
        loadMessages();
    }
}
