package com.example;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.example.DAO.User;

public class RegisterController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label errorMessage;

    @FXML
    private void handleRegisterButtonAction() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            errorMessage.setText("All fields must be filled.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            errorMessage.setText("Passwords do not match.");
            return;
        }

        if (isUsernameTaken(username)) {
            errorMessage.setText("Username already exists. Please choose another one.");
            return;
        }

        if (User.registerUser(username, password)) {
            errorMessage.setText("User successfully registered.");
        } else {
            errorMessage.setText("Registration failed. Please try again.");
        }
    }

    @FXML
    private void handleAlreadyHaveAccountClick() {
        try {
            App.setRoot("/com/example/login.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isUsernameTaken(String username) {
        String query = "SELECT COUNT(*) FROM userlist WHERE user_name = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next() && resultSet.getInt(1) > 0) {
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }
}
