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

    public static int createProject(String projectName, String projectDescription, int userId) {
        String query = "INSERT INTO projects (proj_name, proj_desc) VALUES (?, ?)";
        int projectId = -1;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, projectName);
            statement.setString(2, projectDescription);
            statement.executeUpdate();

            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                projectId = generatedKeys.getInt(1);
                linkProjectToUser(projectId, userId);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return projectId;
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

    public static void deleteProject(int projectId) {
        String query = "DELETE FROM projects WHERE proj_id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, projectId);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void addTemplateOne(int projectId) {
        Tags.createGroup(projectId, 1,  "Group 1", 1);
        Tags.createGroup(projectId, 2, "Group 2", 2);
    }

    public static void addTemplateTwo(int projectId) {
        Tags.createGroup(projectId, 1, "Group 3", 3);
    }
}
