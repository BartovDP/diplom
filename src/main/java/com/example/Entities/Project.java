package com.example.Entities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.example.DatabaseManager;

public class Project {

    public static List<ProjectDetails> getProjectsForUser(String username) {
        String query = "SELECT projects.proj_id, projects.proj_name, projects.proj_desc " +
                "FROM projects " +
                "JOIN pu_connector ON projects.proj_id = pu_connector.proj_id " +
                "JOIN userlist ON pu_connector.user_id = userlist.user_id " +
                "WHERE userlist.user_name = ?";
        List<ProjectDetails> projects = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int projectId = resultSet.getInt("proj_id");
                String projectName = resultSet.getString("proj_name");
                String projectDescription = resultSet.getString("proj_desc");
                projects.add(new ProjectDetails(projectId, projectName, projectDescription));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return projects;
    }

    public static void createProject(String projectName, String projectDescription, int userId) {
        String query = "INSERT INTO projects (proj_name, proj_desc) VALUES (?, ?)";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, projectName);
            statement.setString(2, projectDescription);
            statement.executeUpdate();

            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                int projectId = generatedKeys.getInt(1);
                // Link the project to the user
                linkProjectToUser(projectId, userId);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void linkProjectToUser(int projectId, int userId) {
        String query = "INSERT INTO pu_connector (proj_id, user_id) VALUES (?, ?)";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, projectId);
            statement.setInt(2, userId);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
