package com.example.guiex1.controller;

import com.example.guiex1.domain.User;
import com.example.guiex1.services.FriendshipRequestService;
import com.example.guiex1.services.FriendshipService;
import com.example.guiex1.services.MessageService;
import com.example.guiex1.services.UserService;
import com.example.guiex1.controller.UserCard;
import com.example.guiex1.utils.observer.Observer2;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class MakeFriendsController implements Observer2 {
    @FXML
    private FlowPane invitesContainer;
    @FXML
    private FlowPane nonFriendsContainer;
    @FXML
    private Button backArrow;

    private UserService userService;
    private FriendshipService friendshipService;
    private User currentUser;
    private MessageService messageService;
    private FriendshipRequestService friendshipRequestService;

    public void setServices(FriendshipService friendshipService, UserService userService, MessageService messageService, FriendshipRequestService friendshipRequestService) {
        this.friendshipService = friendshipService;
        this.userService = userService;
        this.messageService = messageService;
        this.friendshipRequestService = friendshipRequestService;
        this.friendshipService.addObserver(this);
        this.friendshipRequestService.addObserver(this);
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
        loadInvites();
        loadNonFriends();
    }

    @FXML
    void initialize() {
        backArrow.setOnAction(event -> goToFriendsPage());
    }

    private void loadInvites() {
        List<User> invites = friendshipRequestService.getReceivedInvites(currentUser);
        invitesContainer.getChildren().clear();
        invites.forEach(user -> {
            UserCard card = new UserCard(
                    user,
                    "Accept",
                    () -> friendshipService.addFriendship(currentUser.getId(), user.getId()),
                    "Decline",
                    () -> friendshipRequestService.rejectFriendInvite(currentUser.getId(), user.getId())
            );
            invitesContainer.getChildren().add(card);
        });
    }

    private void loadNonFriends() {
        List<User> nonFriends = friendshipService.getNotFriendsForUser(currentUser);
        nonFriendsContainer.getChildren().clear();
        nonFriends.forEach(user -> {
            UserCard card = new UserCard(
                    user,
                    "Add Friend",
                    () -> friendshipRequestService.sendFriendInvite(currentUser.getId(), user.getId())
            );
            nonFriendsContainer.getChildren().add(card);
        });
    }

    private void goToFriendsPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/guiex1/views/friends-page-view.fxml"));
            Parent friendsPage = loader.load();
            FriendsPageController friendsPageController = loader.getController();
            friendsPageController.setServices(friendshipService, userService, messageService, friendshipRequestService);
            friendsPageController.setUser(currentUser);
            Scene scene = new Scene(friendsPage, 1200, 900);
            Stage stage = (Stage) backArrow.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update() {
        loadInvites();
        loadNonFriends();
    }
}
