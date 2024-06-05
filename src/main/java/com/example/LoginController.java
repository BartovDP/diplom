package com.example;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginController {  
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button confirmButton;

    @FXML
    private Label errorMessageLabel;

    @FXML
    private CheckBox rememberMeCheckBox;

    private static String username;
    private String password;
    private boolean rememberMe;

    @FXML
    private void handleLogin() {
        // Сохраняем введенные данные в переменные
        username = usernameField.getText();
        password = passwordField.getText();
        rememberMe = rememberMeCheckBox.isSelected();
        
        // Выводим данные для проверки (можно удалить позже)
        System.out.println("Username: " + username);
        System.out.println("Password: " + password);

        // Placeholder for actual login logic
        if (validateLogin(username, password)) {
            openMainInterface();
        } else {
            // Обработка неудачной попытки логина
            errorMessageLabel.setText("Invalid username or password. Please try again.");
        }

    }

    @FXML
    private void handleCreateAccountClick() {
        try {
            App.setRoot("/com/example/register.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean validateLogin(String username, String password) {
        String query = "SELECT user_pass FROM userlist WHERE user_name = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String storedHashedPassword = resultSet.getString("user_pass");
                return PasswordUtils.checkPassword(password, storedHashedPassword);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    private void openMainInterface() {
        try {
            App.setRoot("/com/example/main.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean isRememberMe() {
        return rememberMe;
    }
}
