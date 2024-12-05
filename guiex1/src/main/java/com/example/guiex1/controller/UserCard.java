package com.example.guiex1.controller;

import com.example.guiex1.domain.User;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

/**
 * A card component for displaying user information with actions (Accept/Decline/Add Friend).
 */
public class UserCard extends VBox {

    private final User user;
    private final Button primaryButton;
    private Button secondaryButton;

    /**
     * Constructor for cards with two buttons (e.g., Accept/Decline for invites).
     */
    public UserCard(User user, String primaryAction, Runnable primaryHandler, String secondaryAction, Runnable secondaryHandler) {
        this.user = user;
        this.primaryButton = new Button(primaryAction);
        this.secondaryButton = new Button(secondaryAction);

        // Set up the user details
        Text name = new Text(user.getFirstName() + " " + user.getLastName());
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Text email = new Text(user.getEmail());
        email.setStyle("-fx-text-fill: gray;");

        // Set up the buttons
        primaryButton.setOnAction(event -> primaryHandler.run());
        secondaryButton.setOnAction(event -> secondaryHandler.run());

        // Layout the card
        HBox buttonContainer = new HBox(10, primaryButton, secondaryButton);
        buttonContainer.setStyle("-fx-alignment: center;");

        this.setSpacing(10);
        this.setStyle("-fx-padding: 15; -fx-background-color: #455a64; -fx-border-radius: 10; -fx-background-radius: 10;");
        this.getChildren().addAll(name, email, buttonContainer);
    }

    /**
     * Constructor for cards with a single button (e.g., Add Friend).
     */
    public UserCard(User user, String primaryAction, Runnable primaryHandler) {
        this.user = user;
        this.primaryButton = new Button(primaryAction);

        // Set up the user details
        Text name = new Text(user.getFirstName() + " " + user.getLastName());
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Text email = new Text(user.getEmail());
        email.setStyle("-fx-text-fill: gray;");

        // Set up the button
        primaryButton.setOnAction(event -> primaryHandler.run());

        // Layout the card
        HBox buttonContainer = new HBox(primaryButton);
        buttonContainer.setStyle("-fx-alignment: center;");

        this.setSpacing(10);
        this.setStyle("-fx-padding: 15; -fx-background-color: #455a64; -fx-border-radius: 10; -fx-background-radius: 10;");
        this.getChildren().addAll(name, email, buttonContainer);
    }

    public User getUser() {
        return user;
    }
}
