package com.example.guiex1;

import com.example.guiex1.controller.LoginPageController;
import com.example.guiex1.domain.*;
import com.example.guiex1.domain.validators.FriendRequestsValidator;
import com.example.guiex1.domain.validators.FriendshipValidator;
import com.example.guiex1.domain.validators.UserValidator;
import com.example.guiex1.repository.Repository;
import com.example.guiex1.repository.dbrepo.*;
import com.example.guiex1.repository.paging.PagingRepository;
import com.example.guiex1.services.FriendshipRequestService;
import com.example.guiex1.services.FriendshipService;
import com.example.guiex1.services.MessageService;
import com.example.guiex1.services.UserService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        String username="postgres";
        String pasword="admin";
        String url="jdbc:postgresql://localhost:5432/socialnetwork";

        Repository<Long, User> userRepository = new UserDbRepository(url,username, pasword,  new UserValidator());
        Repository<Tuple<Long, Long>, Friendship> friendshipRepository = new FriendshipDBRepository(new FriendshipValidator());
        PagingRepository<Tuple<Long, Long>, Friendship> friendshipPagingRepo = new FriendshipPagingRepository(new FriendshipValidator());
        Repository<Tuple<Long, Long>, FriendRequests> friendRequestsRepository = new FriendRequestsDBRepository(new FriendRequestsValidator());
        Repository<Long, Message> messageRepository = new MessageDBRepository(userRepository);

        UserService userService = new UserService(userRepository);
        FriendshipService friendshipService = new FriendshipService(friendshipPagingRepo, friendshipRepository, userRepository, friendRequestsRepository);
        MessageService messageService = new MessageService(messageRepository, userRepository);
        FriendshipRequestService friendshipRequestService = new FriendshipRequestService(userRepository, messageRepository, friendRequestsRepository);

        initView(primaryStage, userService, friendshipService, messageService, friendshipRequestService);
        primaryStage.setWidth(1200);
        primaryStage.show();
    }

    private void initView(Stage primaryStage, UserService userService, FriendshipService friendshipService, MessageService messageService, FriendshipRequestService friendshipRequestService) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("views/login-page-view.fxml"));

        StackPane userLayout = fxmlLoader.load();
        primaryStage.setScene(new Scene(userLayout));

        LoginPageController welcomePage = fxmlLoader.getController();
        welcomePage.setServices(friendshipService, userService, messageService, friendshipRequestService);
    }
}