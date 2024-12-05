package com.example.guiex1;

import com.example.guiex1.controller.LoginPageController;
import com.example.guiex1.domain.*;
import com.example.guiex1.domain.validators.FriendRequestsValidator;
import com.example.guiex1.domain.validators.FriendshipValidator;
import com.example.guiex1.domain.validators.UserValidator;
import com.example.guiex1.repository.Repository;
import com.example.guiex1.repository.dbrepo.FriendRequestsDBRepository;
import com.example.guiex1.repository.dbrepo.FriendshipDBRepository;
import com.example.guiex1.repository.dbrepo.MessageDBRepository;
import com.example.guiex1.repository.dbrepo.UserDbRepository;
import com.example.guiex1.services.FriendshipService;
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

        System.out.println("Reading data from file");
        String username="postgres";
        String pasword="admin";
        String url="jdbc:postgresql://localhost:5432/socialnetwork";
        Repository<Long, User> utilizatorRepository =
                new UserDbRepository(url,username, pasword,  new UserValidator());
        Repository<Tuple<Long, Long>, Friendship> friendshipRepository = new FriendshipDBRepository(new FriendshipValidator());
        Repository<Tuple<Long, Long>, FriendRequests> friendRequestsRepository = new FriendRequestsDBRepository(new FriendRequestsValidator());
        Repository<Long, Message> messageRepository = new MessageDBRepository(new FriendRequestsValidator(), utilizatorRepository);

        UserService userService = new UserService(utilizatorRepository, messageRepository);
        FriendshipService friendshipService = new FriendshipService(friendshipRepository, utilizatorRepository, friendRequestsRepository, messageRepository);
        initView(primaryStage, userService, friendshipService);
        primaryStage.setWidth(1200);
        primaryStage.show();
    }

    private void initView(Stage primaryStage, UserService userService, FriendshipService friendshipService) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("views/login-page-view.fxml"));

        StackPane userLayout = fxmlLoader.load();
        primaryStage.setScene(new Scene(userLayout));

        LoginPageController welcomePage = fxmlLoader.getController();
        welcomePage.setUserService(userService);
        welcomePage.setFriendshipService(friendshipService);
    }
}