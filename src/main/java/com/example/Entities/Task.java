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
}
