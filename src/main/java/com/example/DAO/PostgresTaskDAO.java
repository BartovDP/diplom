package com.example.DAO;

import com.example.ColorUtils;
import com.example.DatabaseManager;
import com.example.Entities.TaskDetails;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PostgresTaskDAO implements TaskDAO {

    @Override
    public int saveTask(int projId, String taskName, String taskDesc, LocalDate taskBeg, LocalDate taskEnd, String taskStatus, String taskColor) {
        String query = "INSERT INTO tasklist (proj_id, task_name, task_desc, task_beg, task_end, task_status, task_color) VALUES (?, ?, ?, ?, ?, ?, ?)";
        int taskId = -1;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, projId);
            statement.setString(2, taskName);
            statement.setString(3, taskDesc);
            statement.setDate(4, java.sql.Date.valueOf(taskBeg));
            statement.setDate(5, java.sql.Date.valueOf(taskEnd));
            statement.setString(6, taskStatus);
            statement.setString(7, taskColor);

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

    @Override
    public void updateTask(String currentTaskName, String newTaskName, String taskDesc, LocalDate taskBeg, LocalDate taskEnd, String taskStatus, String taskColor) {
        String query = "UPDATE tasklist SET task_name = ?, task_desc = ?, task_beg = ?, task_end = ?, task_status = ?, task_color = ? WHERE task_name = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, newTaskName);
            statement.setString(2, taskDesc);
            statement.setDate(3, java.sql.Date.valueOf(taskBeg));
            statement.setDate(4, java.sql.Date.valueOf(taskEnd));
            statement.setString(5, taskStatus);
            statement.setString(6, taskColor);
            statement.setString(7, currentTaskName);

            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteTask(String taskName) {
        String query = "DELETE FROM tasklist WHERE task_name = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, taskName);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public TaskDetails getTaskDetails(String taskName) {
        String query = "SELECT task_name, task_desc, task_beg, task_end, task_status, task_color FROM tasklist WHERE task_name = ?";
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
                String taskStatus = resultSet.getString("task_status");
                String taskColor = ColorUtils.convertDbColorToCss(resultSet.getString("task_color"));
                taskDetails = new TaskDetails(name, description, beginningDate, endingDate, taskStatus, taskColor);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return taskDetails;
    }

    @Override
    public int getTaskId(String taskName) {
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

    @Override
    public void saveResponsibleUser(int taskId, int userId) {
        String query = "INSERT INTO task_connector (task_id, user_id) VALUES (?, ?)";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, taskId);
            statement.setInt(2, userId);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateResponsibleUser(int taskId, int userId) {
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

    @Override
    public List<TaskDetails> getTasksForProject(int projectId) {
        String query = "SELECT task_name, task_desc, task_beg, task_end, task_status, task_color FROM tasklist WHERE proj_id = ?";
        List<TaskDetails> tasks = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, projectId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String taskName = resultSet.getString("task_name");
                String taskDescription = resultSet.getString("task_desc");
                LocalDate beginningDate = resultSet.getDate("task_beg").toLocalDate();
                LocalDate endingDate = resultSet.getDate("task_end").toLocalDate();
                String taskStatus = resultSet.getString("task_status");
                String taskColor = ColorUtils.convertDbColorToCss(resultSet.getString("task_color"));
                tasks.add(new TaskDetails(taskName, taskDescription, beginningDate, endingDate, taskStatus, taskColor));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return tasks;
    }

    @Override
    public List<TaskDetails> getTasksForUser(int userId) {
        String query = "SELECT t.task_name, t.task_desc, t.task_beg, t.task_end, t.task_status, t.task_color FROM tasklist t " +
                "JOIN task_connector tc ON t.task_id = tc.task_id " +
                "WHERE tc.user_id = ?";
        List<TaskDetails> tasks = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, userId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String taskName = resultSet.getString("task_name");
                String taskDescription = resultSet.getString("task_desc");
                LocalDate beginningDate = resultSet.getDate("task_beg").toLocalDate();
                LocalDate endingDate = resultSet.getDate("task_end").toLocalDate();
                String taskStatus = resultSet.getString("task_status");
                String taskColor = ColorUtils.convertDbColorToCss(resultSet.getString("task_color"));
                tasks.add(new TaskDetails(taskName, taskDescription, beginningDate, endingDate, taskStatus, taskColor));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return tasks;
    }

    @Override
    public List<TaskDetails> getTasksForGroup(int projId, int tagId, int groupId) {
        String query = "SELECT DISTINCT t.task_name, t.task_desc, t.task_beg, t.task_end, t.task_status, t.task_color " +
                "FROM tasklist t " +
                "JOIN tag_connector tc ON t.task_id = tc.task_id " +
                "JOIN taglist tl ON tc.tag_id = tl.tag_id " +
                "JOIN grouplist g ON tl.tag_id = g.tag_id " +
                "WHERE g.tag_id = ? AND t.proj_id = ? AND g.group_id = ?";
        List<TaskDetails> tasks = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, tagId);
            statement.setInt(2, projId);
            statement.setInt(3, groupId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String taskName = resultSet.getString("task_name");
                String taskDescription = resultSet.getString("task_desc");
                LocalDate beginningDate = resultSet.getDate("task_beg").toLocalDate();
                LocalDate endingDate = resultSet.getDate("task_end").toLocalDate();
                String taskStatus = resultSet.getString("task_status");
                String taskColor = ColorUtils.convertDbColorToCss(resultSet.getString("task_color"));
                tasks.add(new TaskDetails(taskName, taskDescription, beginningDate, endingDate, taskStatus, taskColor));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return tasks;
    }
}
