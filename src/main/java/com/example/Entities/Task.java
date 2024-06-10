package com.example.Entities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import com.example.DatabaseManager;

public class Task {

    public static int saveTask(int projId, String taskName, String taskDesc, LocalDate taskBeg, LocalDate taskEnd) {
        String query = "INSERT INTO tasklist (proj_id, task_name, task_desc, task_beg, task_end) VALUES (?, ?, ?, ?, ?)";
        int taskId = -1;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, projId);
            statement.setString(2, taskName);
            statement.setString(3, taskDesc);
            statement.setDate(4, java.sql.Date.valueOf(taskBeg));
            statement.setDate(5, java.sql.Date.valueOf(taskEnd));

            statement.executeUpdate();

            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                taskId = generatedKeys.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return taskId;
    }

    public static void updateTask(String currentTaskName, String newTaskName, String taskDesc, LocalDate taskBeg, LocalDate taskEnd) {
        String query = "UPDATE tasklist SET task_name = ?, task_desc = ?, task_beg = ?, task_end = ? WHERE task_name = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, newTaskName);
            statement.setString(2, taskDesc);
            statement.setDate(3, java.sql.Date.valueOf(taskBeg));
            statement.setDate(4, java.sql.Date.valueOf(taskEnd));
            statement.setString(5, currentTaskName);

            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteTask(String taskName) {
        String query = "DELETE FROM tasklist WHERE task_name = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, taskName);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static TaskDetails getTaskDetails(String taskName) {
        String query = "SELECT task_name, task_desc, task_beg, task_end FROM tasklist WHERE task_name = ?";
        TaskDetails taskDetails = null;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, taskName);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String name = resultSet.getString("task_name");
                String description = resultSet.getString("task_desc");
                LocalDate beginningDate = resultSet.getDate("task_beg").toLocalDate();
                LocalDate endingDate = resultSet.getDate("task_end").toLocalDate();
                taskDetails = new TaskDetails(name, description, beginningDate, endingDate);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return taskDetails;
    }

    public static int getTaskId(String taskName) {
        String query = "SELECT task_id FROM tasklist WHERE task_name = ?";
        int taskId = -1;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, taskName);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                taskId = resultSet.getInt("task_id");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return taskId;
    }

    public static void saveResponsibleUser(int taskId, int userId) {
        String query = "INSERT INTO task_connector (task_id, user_id) " +
                       "VALUES (?, ?)";
    
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
    
            statement.setInt(1, taskId);
            statement.setInt(2, userId);
            statement.executeUpdate();
    
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateResponsibleUser(int taskId, int userId) {
        String query = "UPDATE task_connector SET user_id = ? WHERE task_id = ?";
    
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
    
            statement.setInt(1, userId);
            statement.setInt(2, taskId);
            statement.executeUpdate();
    
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
