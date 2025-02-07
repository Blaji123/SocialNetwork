package com.example.guiex1.controller;

import com.example.guiex1.domain.User;
import com.example.guiex1.services.FriendshipRequestService;
import com.example.guiex1.services.FriendshipService;
import com.example.guiex1.services.MessageService;
import com.example.guiex1.services.UserService;
import com.example.guiex1.utils.events.UserEntityChangeEvent;
import com.example.guiex1.utils.observer.Observer;
import com.example.guiex1.utils.observer.Observer2;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Objects;

public class ProfilePageController implements Observer<UserEntityChangeEvent>, Observer2 {
    @FXML
    private ImageView userPhoto;
    @FXML
    private Label userNameLabel;
    @FXML
    private Label postsCountLabel;
    @FXML
    private Label friendsCountLabel;
    @FXML
    private Button backButton;
    @FXML
    private ImageView noPostsImage;
    @FXML
    private Button dynamicButton;

    private User targetUser;
    private User currentUser;
    private UserService userService;
    private FriendshipService friendshipService;
    private MessageService messageService;
    private FriendshipRequestService friendshipRequestService;

    public void setServices(FriendshipService friendshipService, UserService userService, MessageService messageService, FriendshipRequestService friendshipRequestService) {
        this.friendshipService = friendshipService;
        this.userService = userService;
        this.messageService = messageService;
        this.friendshipRequestService = friendshipRequestService;
        this.userService.addObserver(this);
        this.friendshipService.addObserver(this);
        this.friendshipRequestService.addObserver(this);
        updateProfileDetails();
    }

    public void setTargetUser(User targetUser) {
        this.targetUser = targetUser;
        updateProfileDetails();
        setDynamicButton();
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    @FXML
    public void initialize() {
        setNoPostsImage();
        setupActions();
    }

    private void setNoPostsImage() {
        try {
            Image cameraIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/guiex1/images/no-posts-image.png")));
            noPostsImage.setImage(cameraIcon);
        } catch (Exception e) {
            System.out.println("Failed to load 'No Posts' image: " + e.getMessage());
        }
    }

    private void setDynamicButton() {
        if(!targetUser.equals(currentUser)) {
            try{
                dynamicButton.setPrefSize(100, 100);
                Image message = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/guiex1/images/message.png")));
                ImageView messageView = new ImageView(message);
                messageView.setFitWidth(100);
                messageView.setPreserveRatio(true);
                messageView.setSmooth(true);
                dynamicButton.setGraphic(messageView);
                dynamicButton.setOnAction(event -> navigateToMessagePage());
            }catch (Exception e){
                System.out.println("Failed to load notification: " + e.getMessage());
            }
        }else{
            try{
                dynamicButton.setPrefSize(100, 100);
                Image message = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/guiex1/images/edit-profile.png")));
                ImageView messageView = new ImageView(message);
                messageView.setFitWidth(100);
                messageView.setPreserveRatio(true);
                messageView.setSmooth(true);
                dynamicButton.setGraphic(messageView);
                dynamicButton.setOnAction(event -> navigateToEditPage());
            }catch (Exception e){
                System.out.println("Failed to load notification: " + e.getMessage());
            }
        }
    }

    private void navigateToEditPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/guiex1/views/update-page-view.fxml"));
            Parent updatePage = loader.load();
            UpdatePageController updatePageController = loader.getController();
            updatePageController.setServices(friendshipService, userService, messageService, friendshipRequestService);
            updatePageController.setUser(currentUser);
            Scene scene = new Scene(updatePage, 1200, 900);
            Stage stage = (Stage) dynamicButton.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void navigateToMessagePage() {
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/guiex1/views/message-page-view.fxml"));
            Parent messagePage = loader.load();
            MessagePageController messagePageController = loader.getController();
            messagePageController.setServices(friendshipService, userService, messageService, friendshipRequestService);
            messagePageController.setCurrentUser(currentUser);
            Scene scene = new Scene(messagePage, 1200, 900);
            Stage stage = (Stage) dynamicButton.getScene().getWindow();
            stage.setScene(scene);
        }catch (Exception e){
            System.out.println("Failed to load friends: " + e.getMessage());
        }
    }

    private void updateProfileDetails() {
        if (targetUser != null) {
            byte[] photoBytes = targetUser.getPhoto();
            Circle circle = new Circle(75, 75, 75);
            userPhoto.setImage(new Image(new ByteArrayInputStream(photoBytes)));
            userPhoto.setClip(circle);
            userNameLabel.setText(targetUser.getFirstName() + " " + targetUser.getLastName());
            postsCountLabel.setText("Posts: 0");

            if (friendshipService != null) {
                int friendsCount = friendshipService.getFriendsForUser(targetUser).size();
                friendsCountLabel.setText("Friends: " + friendsCount);
            }
        }
    }

    private void setupActions() {
        backButton.setOnAction(event -> navigateToFriendsPage());
    }

    private void navigateToFriendsPage() {
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
            e.printStackTrace();
        }
    }

    @Override
    public void update(UserEntityChangeEvent userEntityChangeEvent) {
        updateProfileDetails();
    }

    @Override
    public void update() {
        updateProfileDetails();
    }
}
