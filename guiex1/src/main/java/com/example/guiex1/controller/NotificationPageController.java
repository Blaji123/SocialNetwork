package com.example.guiex1.controller;

import com.example.guiex1.domain.Message;
import com.example.guiex1.domain.User;
import com.example.guiex1.services.FriendshipService;
import com.example.guiex1.services.UserService;
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

public class NotificationPageController implements Observer2 {

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

    public void initialize() {
        // Configure table columns
        setupTableColumns();
        setBackArrow();

        // Bind data to the table
        notificationTable.setItems(notificationObservableList);

        // Set back button action
        backButton.setOnAction(event -> goBack());
    }

    private void setupTableColumns() {
        // Set cell value factories for columns
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

    public void setFriendshipService(FriendshipService friendshipService) {
        this.friendshipService = friendshipService;
        friendshipService.addObserver(this);
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void setUser(User user) {
        this.user = user;
        loadNotifications(); // Load notifications for the user
    }

    /**
     * Loads notifications from the service and updates the UI.
     */
    private void loadNotifications() {
        List<Message> notifications = userService.getNotifications(user.getId());
        notificationObservableList.setAll(notifications);
    }

    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/guiex1/views/friends-page-view.fxml"));
            Parent friendsPage = loader.load();
            FriendsPageController friendsPageController = loader.getController();
            friendsPageController.setUserService(userService);
            friendsPageController.setFriendshipService(friendshipService);
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
        loadNotifications(); // Reload notifications on update
    }
}
