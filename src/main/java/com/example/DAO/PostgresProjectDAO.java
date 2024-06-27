package com.example.DAO;

import com.example.ColorUtils;
import com.example.DatabaseManager;
import com.example.Entities.ProjectDetails;
import com.example.PasswordUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PostgresProjectDAO implements ProjectDAO {

    @Override
    public List<ProjectDetails> getProjectsForUser(String username) {
        String query = "SELECT projects.proj_id, projects.proj_name, projects.proj_desc, projects.proj_template, projects.share_key, projects.proj_group, projects.proj_color " +
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
                String projectTemplate = resultSet.getString("proj_template");
                String shareKey = resultSet.getString("share_key");
                String projectGroup = resultSet.getString("proj_group");
                String projectColor = ColorUtils.convertDbColorToCss(resultSet.getString("proj_color"));
                projects.add(new ProjectDetails(projectId, projectName, projectDescription, projectTemplate, shareKey, projectGroup, projectColor));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return projects;
    }

    @Override
    public int createProject(String projectName, String projectDescription, int userId, String template, String projectGroup, String projectColor) {
        String query = "INSERT INTO projects (proj_name, proj_desc, proj_template, share_key, proj_group, proj_color) VALUES (?, ?, ?, ?, ?, ?)";
        int projectId = -1;
        String shareKey = PasswordUtils.hashPassword(projectName + userId);

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, projectName);
            statement.setString(2, projectDescription);
            statement.setString(3, template);
            statement.setString(4, shareKey);
            statement.setString(5, projectGroup);
            statement.setString(6, projectColor);
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

    @Override
    public void linkProjectToUser(int projectId, int userId) {
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

    @Override
    public void deleteProject(int projectId) {
        String query = "DELETE FROM projects WHERE proj_id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, projectId);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public ProjectDetails getProjectDetails(int projectId) {
        String query = "SELECT proj_name, proj_desc, proj_template, share_key, proj_group, proj_color FROM projects WHERE proj_id = ?";
        ProjectDetails projectDetails = null;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, projectId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String projectName = resultSet.getString("proj_name");
                String projectDescription = resultSet.getString("proj_desc");
                String projectTemplate = resultSet.getString("proj_template");
                String shareKey = resultSet.getString("share_key");
                String projectGroup = resultSet.getString("proj_group");
                String projectColor = ColorUtils.convertDbColorToCss(resultSet.getString("proj_color"));
                projectDetails = new ProjectDetails(projectId, projectName, projectDescription, projectTemplate, shareKey, projectGroup, projectColor);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return projectDetails;
    }

    @Override
    public List<String> getUsersWithAccess(int projectId) {
        String query = "SELECT userlist.user_name FROM userlist " +
                "JOIN pu_connector ON userlist.user_id = pu_connector.user_id " +
                "WHERE pu_connector.proj_id = ?";
        List<String> users = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, projectId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                users.add(resultSet.getString("user_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return users;
    }

    @Override
    public String getShareKey(int projectId) {
        String query = "SELECT share_key FROM projects WHERE proj_id = ?";
        String shareKey = null;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, projectId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                shareKey = resultSet.getString("share_key");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return shareKey;
    }

    @Override
    public int getProjectIdByKey(String shareKey) {
        String query = "SELECT proj_id FROM projects WHERE share_key = ?";
        int projectId = -1;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, shareKey);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                projectId = resultSet.getInt("proj_id");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return projectId;
    }

    @Override
    public List<String> getUserProjectGroups(String username) {
        String query = "SELECT DISTINCT p.proj_group " +
                       "FROM projects p " +
                       "JOIN pu_connector pc ON p.proj_id = pc.proj_id " +
                       "JOIN userlist u ON pc.user_id = u.user_id " +
                       "WHERE u.user_name = ?";
        List<String> projectGroups = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                projectGroups.add(resultSet.getString("proj_group"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return projectGroups;
    }

    @Override
    public List<ProjectDetails> getProjectsForUserByGroup(String username, String group) {
        String query = "SELECT p.proj_id, p.proj_name, p.proj_desc, p.proj_template, p.proj_group, p.proj_color " +
                       "FROM projects p " +
                       "JOIN pu_connector pc ON p.proj_id = pc.proj_id " +
                       "JOIN userlist u ON pc.user_id = u.user_id " +
                       "WHERE u.user_name = ? AND p.proj_group = ?";
        List<ProjectDetails> projects = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, username);
            statement.setString(2, group);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int projectId = resultSet.getInt("proj_id");
                String projectName = resultSet.getString("proj_name");
                String projectDescription = resultSet.getString("proj_desc");
                String projectTemplate = resultSet.getString("proj_template");
                String projectGroup = resultSet.getString("proj_group");
                String projectColor = resultSet.getString("proj_color");

                projects.add(new ProjectDetails(projectId, projectName, projectDescription, projectTemplate, projectGroup, projectColor));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return projects;
    }

    @Override
    public int getTaskCountForProject(int projectId) {
        String query = "SELECT COUNT(*) AS task_count FROM tasklist WHERE proj_id = ?";
        int taskCount = 0;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, projectId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                taskCount = resultSet.getInt("task_count");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println("Total tasks: " +taskCount);

        return taskCount;
    }

    @Override
    public int getCompletedTaskCountForProject(int projectId) {
        String query = "SELECT COUNT(*) AS task_count FROM tasklist WHERE proj_id = ? AND task_status = 'Выполнено'";
        int taskCount = 0;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, projectId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                taskCount = resultSet.getInt("task_count");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println("Completed tasks:" + taskCount);

        return taskCount;
    }
}
