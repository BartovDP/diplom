package com.example.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.example.DatabaseManager;
import com.example.PasswordUtils;

public class User {

    public static boolean registerUser(String username, String password) {
        String query = "INSERT INTO userlist (user_name, user_pass) VALUES (?, ?)";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, username);
            statement.setString(2, PasswordUtils.hashPassword(password));
            int rowsInserted = statement.executeUpdate();

            return rowsInserted > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static String getResponsibleUser(String taskName) {
        String query = "SELECT u.user_name " +
                       "FROM userlist u " +
                       "JOIN task_connector tc ON u.user_id = tc.user_id " +
                       "JOIN tasklist t ON tc.task_id = t.task_id " +
                       "WHERE t.task_name = ?";
        String responsibleUser = null;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, taskName);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                responsibleUser = resultSet.getString("user_name");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return responsibleUser;
    }
}
