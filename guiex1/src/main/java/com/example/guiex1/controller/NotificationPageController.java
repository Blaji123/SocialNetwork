package com.example.guiex1.controller;

import com.example.guiex1.domain.Message;
import com.example.guiex1.domain.User;
import com.example.guiex1.services.FriendshipRequestService;
import com.example.guiex1.services.FriendshipService;
import com.example.guiex1.services.MessageService;
import com.example.guiex1.services.UserService;
import com.example.guiex1.utils.events.Event;
import com.example.guiex1.utils.events.UserEntityChangeEvent;
import com.example.guiex1.utils.observer.Observer;
import com.example.guiex1.utils.observer.Observer2;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class NotificationPageController implements Observer2, Observer<UserEntityChangeEvent> {
    @FXML
    private TableView<Message> notificationTable;
    @FXML
    private TableColumn<Message, String> messageColumn;
    @FXML
    private TableColumn<Message, String> dateColumn;
    @FXML
    private javafx.scene.control.Button backButton;

    private UserService userService;
    private FriendshipService friendshipService;
    private User user;
    private ObservableList<Message> notificationObservableList = FXCollections.observableArrayList();
    private MessageService messageService;
    private FriendshipRequestService friendshipRequestService;

    public void setServices(FriendshipService friendshipService, UserService userService, MessageService messageService, FriendshipRequestService friendshipRequestService) {
        this.friendshipService = friendshipService;
        this.userService = userService;
        this.messageService = messageService;
        this.friendshipRequestService = friendshipRequestService;
        this.messageService.addObserver(this);
        this.friendshipService.addObserver(this);
        this.friendshipRequestService.addObserver(this);
    }

    public void setUser(User user) {
        this.user = user;
        loadNotifications();
    }

    public void initialize() {
        setupTableColumns();
        setBackArrow();
        notificationTable.setItems(notificationObservableList);
        backButton.setOnAction(event -> goBack());
    }

    private void setupTableColumns() {
        messageColumn.setCellValueFactory(new PropertyValueFactory<>("message"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
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
            System.out.println("Error loading back arrow image: " + ex.getMessage());
        }
    }

    private void loadNotifications() {
        List<Message> notifications = messageService.getNotifications(user.getId());
        notificationObservableList.setAll(notifications);
    }

    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/guiex1/views/friends-page-view.fxml"));
            Parent friendsPage = loader.load();
            FriendsPageController friendsPageController = loader.getController();
            friendsPageController.setServices(friendshipService, userService, messageService, friendshipRequestService);
            friendsPageController.setUser(user);
            Scene scene = new Scene(friendsPage, 1200, 900);
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update() {
        loadNotifications();
    }

    @Override
    public void update(UserEntityChangeEvent userEntityChangeEvent) {
        loadNotifications();
    }
}
