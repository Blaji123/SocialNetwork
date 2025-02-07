package com.example.guiex1.controller;

import com.example.guiex1.domain.Message;
import com.example.guiex1.domain.User;
import com.example.guiex1.services.FriendshipRequestService;
import com.example.guiex1.services.FriendshipService;
import com.example.guiex1.services.MessageService;
import com.example.guiex1.services.UserService;
import com.example.guiex1.utils.events.UserEntityChangeEvent;
import com.example.guiex1.utils.observer.Observer;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class MessagePageController implements Observer<UserEntityChangeEvent> {
    @FXML
    private ListView<User> friendsListView;
    @FXML
    private ListView<Message> messagesListView;  // Used instead of messagesListView
    @FXML
    private TextField messageField;
    @FXML
    private Button sendButton;
    @FXML
    private Button backButton;
    @FXML
    private ImageView selectedUserImage; // Profile picture of selected user
    @FXML
    private Label chatTitle;

    private UserService userService;
    private FriendshipService friendshipService;
    private User currentUser;
    private User selectedFriend;
    private MessageService messageService;
    private FriendshipRequestService friendshipRequestService;

    public void setServices(FriendshipService friendshipService, UserService userService, MessageService messageService, FriendshipRequestService friendshipRequestService) {
        this.friendshipService = friendshipService;
        this.userService = userService;
        this.messageService = messageService;
        this.friendshipRequestService = friendshipRequestService;
        this.messageService.addObserver(this);
        this.userService.addObserver(this);
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
                chatTitle.setText("Chat with " + selectedFriend.getFirstName() + " " + selectedFriend.getLastName());

                if (selectedFriend.getPhoto() != null) {
                    selectedUserImage.setImage(new Image(new ByteArrayInputStream(selectedFriend.getPhoto())));
                } else {
                    selectedUserImage.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/guiex1/images/default-profile.png"))));
                }

                loadMessages();
            }
        });

        setBackArrow();
        setupFriendsList();

        sendButton.setOnAction(event -> sendMessage());
        backButton.setOnAction(event -> goToFriendsPage());

        messageField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                sendMessage();
            }
        });

        setupMessagesList();
    }

    private void setupMessagesList() {
        messagesListView.setCellFactory(param -> new ListCell<>() {
            private final HBox container = new HBox();
            private final Label messageLabel = new Label();

            {
                messageLabel.setWrapText(true);
                messageLabel.setMaxWidth(400); // Limit width
                container.getChildren().add(messageLabel);
            }

            @Override
            protected void updateItem(Message message, boolean empty) {
                super.updateItem(message, empty);
                if (empty || message == null) {
                    setGraphic(null);
                } else {
                    messageLabel.setText(message.getMessage());

                    if (message.getFrom().getId().equals(currentUser.getId())) {
                        // Current user messages (right)
                        container.setAlignment(Pos.CENTER_RIGHT);
                        messageLabel.setStyle("-fx-background-color: #0b93f6; -fx-text-fill: white; -fx-padding: 8 12; -fx-background-radius: 10;");
                    } else {
                        // Friend messages (left)
                        container.setAlignment(Pos.CENTER_LEFT);
                        messageLabel.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: black; -fx-padding: 8 12; -fx-background-radius: 10;");
                    }

                    setGraphic(container);
                }
            }
        });
    }

    private void setBackArrow() {
        try {
            backButton.setPrefSize(50, 50);
            Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/guiex1/images/backArrow.png")));
            ImageView imageView = new ImageView(image);
            imageView.setFitHeight(50);
            imageView.setPreserveRatio(true);
            backButton.setGraphic(imageView);
        } catch (Exception ex) {
            System.out.println("Error loading back arrow: " + ex.getMessage());
        }
    }

    private void setupFriendsList() {
        friendsListView.setCellFactory(param -> new ListCell<User>() {
            private final HBox container = new HBox(10);
            private final ImageView profilePicture = new ImageView();
            private final Label nameLabel = new Label();

            {
                profilePicture.setFitWidth(40);
                profilePicture.setFitHeight(40);
                profilePicture.setPreserveRatio(true);
                container.getChildren().addAll(profilePicture, nameLabel);
            }

            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setGraphic(null);
                } else {
                    if (user.getPhoto() != null) {
                        profilePicture.setImage(new Image(new ByteArrayInputStream(user.getPhoto())));
                    } else {
                        profilePicture.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/guiex1/images/default-profile.png"))));
                    }
                    nameLabel.setText(user.getFirstName() + " " + user.getLastName());
                    setGraphic(container);
                }
            }
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
        List<Message> messages = messageService.getMessages(currentUser.getId(), selectedFriend.getId());
        messagesListView.getItems().addAll(messages);
    }

    private void sendMessage() {
        String content = messageField.getText().trim();
        if (!content.isEmpty() && selectedFriend != null) {
            messageService.addMessage(currentUser.getId(), selectedFriend.getId(), content);
            messageField.clear();
            loadMessages();
        }
    }

    @Override
    public void update(UserEntityChangeEvent userEntityChangeEvent) {
        loadMessages();
    }
}
