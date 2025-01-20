package com.example.guiex1.controller;

import com.example.guiex1.domain.User;
import com.example.guiex1.repository.paging.Page;
import com.example.guiex1.services.FriendshipService;
import com.example.guiex1.services.UserService;
import com.example.guiex1.utils.events.ChangeEventType;
import com.example.guiex1.utils.events.UserEntityChangeEvent;
import com.example.guiex1.utils.observer.Observer;
import com.example.guiex1.utils.observer.Observer2;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FriendsPageController implements Observer2, Observer<UserEntityChangeEvent> {

    @FXML
    private Button homeButton;
    @FXML
    private ComboBox<Integer> pageSizeDropdown;
    //    @FXML
//    private TextField searchBar;
    @FXML
    private Button messagesButton;
    @FXML
    private Button notificationsButton;
    @FXML
    private Button logoutButton;
    @FXML
    private Label profileNameLabel;
    @FXML
    private Button viewProfileButton;
    @FXML
    private Button viewProfilePageButton;
    @FXML
    private Button friendsListButton;
    @FXML
    private TableView<User> friendsTable;
    @FXML
    private TableColumn<User, String> friendFirstNameCol;
    @FXML
    private TableColumn<User, String> friendLastNameCol;
    @FXML
    private TableColumn<User, Void> friendActionCol;
    @FXML
    private TableColumn<User, Void> viewProfileCol;
    @FXML
    private Button previousPageButton;
    @FXML
    private Button nextPageButton;
    @FXML
    private Label pageNumberLabel;

    private FriendshipService friendshipService;
    private UserService userService;
    private User user;
    private final ObservableList<User> friendsList = FXCollections.observableArrayList();
    private int currentPage = 0;
    private static int PAGE_SIZE = 5;

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
        this.userService.addObserver(this);
    }

    public FriendshipService getFriendshipService() {
        return friendshipService;
    }

    public void setFriendshipService(FriendshipService friendshipService) {
        this.friendshipService = friendshipService;
        this.friendshipService.addObserver(this);
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
        profileNameLabel.setText(user.getFirstName() + " " + user.getLastName());
        loadFriends();
    }

    /**
     * Initialize the controller. This method is called automatically after the FXML file is loaded.
     */
    @FXML
    public void initialize() {
        setLogoImage();
        setNotifcationImage();
        setMessageImage();
        setupFriendTable();

//        searchBar.setOnKeyReleased(this::handleSearch);
        previousPageButton.setOnAction(event -> loadPreviousPage());
        nextPageButton.setOnAction(event -> loadNextPage());
        messagesButton.setOnAction(event -> navigateToMessages());
        notificationsButton.setOnAction(event -> navigateToNotifications());
        logoutButton.setOnAction(event -> handleLogout());
        viewProfileButton.setOnAction(event -> navigateToProfile());
        friendsListButton.setOnAction(event -> navigateToFriendsList());
        viewProfilePageButton.setOnAction(event -> navigateToViewProfilePage());

        setupPageSizeDropdown();
    }

    private void navigateToViewProfilePage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/guiex1/views/profile-page-view.fxml"));
            Parent profilePage = loader.load();
            ProfilePageController profilePageController = loader.getController();
            profilePageController.setUserService(userService);
            profilePageController.setFriendshipService(friendshipService);
            profilePageController.setCurrentUser(user);
            profilePageController.setTargetUser(user);
            Scene scene = new Scene(profilePage, 1200, 900);
            Stage stage = (Stage) viewProfilePageButton.getScene().getWindow();
            stage.setScene(scene);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void setupPageSizeDropdown() {
        pageSizeDropdown.getItems().addAll(5, 10, 15, 20); // Add predefined page size options.
        pageSizeDropdown.setValue(PAGE_SIZE); // Set default value.
        pageSizeDropdown.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                currentPage = 0; // Reset to the first page.
                PAGE_SIZE = newValue; // Update the page size.
                loadFriends(); // Reload the friends list with the new page size.
            }
        });
    }

    private void setupFriendTable() {
        friendFirstNameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFirstName()));
        friendLastNameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getLastName()));

        friendActionCol.setCellFactory(tc -> new TableCell<>() {
            private final Button removeButton = new Button("Remove");

            {
                removeButton.setOnAction(event -> {
                    User friend = getTableView().getItems().get(getIndex());
                    friendshipService.removeFriendship(user.getId(), friend.getId());
                    updateLists();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : removeButton);
            }
        });

        viewProfileCol.setCellFactory(tc -> new TableCell<>() {
            private final Button viewProfileButton = new Button("View Profile");

            {
                viewProfileButton.setOnAction(event -> {
                    try {
                        User friend = getTableView().getItems().get(getIndex());
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/guiex1/views/profile-page-view.fxml"));
                        Parent profilePage = loader.load();
                        ProfilePageController profilePageController = loader.getController();
                        profilePageController.setUserService(userService);
                        profilePageController.setFriendshipService(friendshipService);
                        profilePageController.setCurrentUser(user);
                        profilePageController.setTargetUser(friend);
                        Scene scene = new Scene(profilePage, 1200, 900);
                        Stage stage = (Stage) viewProfilePageButton.getScene().getWindow();
                        stage.setScene(scene);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : viewProfileButton);
            }

        });

    }

    private void updateLists() {
        // Updates the lists for the tables using Observer or service methods
        loadFriends();
    }

    private void loadFriends() {
        Page<User> page = friendshipService.getPaginatedFriendsForUser(user, currentPage, PAGE_SIZE);
        friendsList.setAll(convertToList(page.getElementsOnPage()));
        friendsTable.setItems(friendsList);
        updatePaginationControls();
    }


    private List<User> convertToList(Iterable<User> elementsOnPage) {
        List<User> list = new ArrayList<>();
        elementsOnPage.forEach(list::add);
        return list;
    }

    private void loadPreviousPage() {
        if(currentPage > 0){
            currentPage--;
            loadFriends();
        }
    }

    private void loadNextPage() {
        if(!friendsList.isEmpty() && friendsList.size() == PAGE_SIZE){
            currentPage++;
            loadFriends();
        }
    }

    private void updatePaginationControls() {
        pageNumberLabel.setText("Page: " + (currentPage + 1));
        previousPageButton.setDisable(currentPage == 0);
        nextPageButton.setDisable(friendsList.size() < PAGE_SIZE);
    }

    private void setMessageImage() {
        try{
            messagesButton.setPrefSize(100, 100);
            Image message = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/guiex1/images/message.png")));
            ImageView messageView = new ImageView(message);
            messageView.setFitWidth(100);
            messageView.setPreserveRatio(true);
            messageView.setSmooth(true);
            messagesButton.setGraphic(messageView);
        }catch (Exception e){
            System.out.println("Failed to load notification: " + e.getMessage());
        }
    }


    private void setNotifcationImage() {
        try{
            notificationsButton.setPrefSize(100, 100);
            Image notification = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/guiex1/images/notification.png")));
            ImageView notificationView = new ImageView(notification);
            notificationView.setFitWidth(100);
            notificationView.setPreserveRatio(true);
            notificationView.setSmooth(true);
            notificationsButton.setGraphic(notificationView);
        }catch (Exception e){
            System.out.println("Failed to load notification: " + e.getMessage());
        }
    }


    private void setLogoImage() {
        try{
            homeButton.setPrefSize(100, 100);
            Image logo = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/guiex1/images/logo.png")));
            ImageView logoView = new ImageView(logo);
            logoView.setFitWidth(100);
            logoView.setPreserveRatio(true);
            homeButton.setGraphic(logoView);
        }catch (Exception e){
            System.out.println("Failed to load logo: " + e.getMessage());
        }
    }

    /**
     * Handle search functionality when typing in the search bar.
     */
//    private void handleSearch(KeyEvent event) {
//        String query = searchBar.getText();
//        System.out.println("Search query: " + query);
//        // Implement search functionality (e.g., filter friends in the table)
//    }

    /**
     * Navigate to the user's profile page.
     */
    private void navigateToProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/guiex1/views/update-page-view.fxml"));
            Parent updatePage = loader.load();
            UpdatePageController updatePageController = loader.getController();
            updatePageController.setUserService(userService);
            updatePageController.setFriendshipService(friendshipService);
            updatePageController.setUser(user);
            Scene scene = new Scene(updatePage, 1200, 900);
            Stage stage = (Stage) viewProfileButton.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Navigate to the user's friends list.
     */
    private void navigateToFriendsList() {
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/guiex1/views/make-friends-view.fxml"));
            Parent makeFriendsPage = loader.load();
            MakeFriendsController makeFriendsController = loader.getController();
            makeFriendsController.setUserService(userService);
            makeFriendsController.setFriendshipService(friendshipService);
            makeFriendsController.setCurrentUser(user);
            Scene scene = new Scene(makeFriendsPage, 1200, 900);
            Stage stage = (Stage) friendsListButton.getScene().getWindow();
            stage.setScene(scene);
        }catch (Exception e){
            System.out.println("Failed to load friends: " + e.getMessage());
        }
    }

    /**
     * Navigate to the messages page.
     */
    private void navigateToMessages() {
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/guiex1/views/message-page-view.fxml"));
            Parent messagePage = loader.load();
            MessagePageController messagePageController = loader.getController();
            messagePageController.setUserService(userService);
            messagePageController.setFriendshipService(friendshipService);
            messagePageController.setCurrentUser(user);
            Scene scene = new Scene(messagePage, 1200, 900);
            Stage stage = (Stage) messagesButton.getScene().getWindow();
            stage.setScene(scene);
        }catch (Exception e){
            System.out.println("Failed to load friends: " + e.getMessage());
        }
    }

    /**
     * Navigate to the notifications page.
     */
    private void navigateToNotifications() {
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/guiex1/views/notification-page-view.fxml"));
            Parent notificationPage = loader.load();
            NotificationPageController notificationPageController = loader.getController();
            notificationPageController.setUserService(userService);
            notificationPageController.setFriendshipService(friendshipService);
            notificationPageController.setUser(user);
            Scene scene = new Scene(notificationPage, 1200, 900);
            Stage stage = (Stage) messagesButton.getScene().getWindow();
            stage.setScene(scene);
        }catch (Exception e){
            System.out.println("Failed to load friends: " + e.getMessage());
        }
    }

    /**
     * Handle the logout action by closing the application window.
     */
    private void handleLogout() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        stage.close();
    }

    @Override
    public void update() {
        updateLists();
    }

    @Override
    public void update(UserEntityChangeEvent userEntityChangeEvent) {
        if(userEntityChangeEvent.getType() == ChangeEventType.UPDATE && user.getId().equals(userEntityChangeEvent.getOldData().getId())) {
            setUser(userEntityChangeEvent.getData());
        }
        updateLists();
    }
}