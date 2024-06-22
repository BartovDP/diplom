package com.example.DAO;

import com.example.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PostgresTagDAO implements TagDAO {

    @Override
    public List<String> getAllTags() {
        String query = "SELECT tag_name FROM taglist";
        List<String> tags = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                tags.add(resultSet.getString("tag_name"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return tags;
    }

    @Override
    public int getTagId(String tagName) {
        String query = "SELECT tag_id FROM taglist WHERE tag_name = ?";
        int tagId = -1;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, tagName);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                tagId = resultSet.getInt("tag_id");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return tagId;
    }

    @Override
    public String getTagNameById(int tagId) {
        String query = "SELECT tag_name FROM taglist WHERE tag_id = ?";
        String tagName = null;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, tagId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                tagName = resultSet.getString("tag_name");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return tagName;
    }

    @Override
    public String getTagColor(String tagName) {
        String query = "SELECT tag_color FROM taglist WHERE tag_name = ?";
        String tagColor = null;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, tagName);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                tagColor = resultSet.getString("tag_color");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return tagColor;
    }

    @Override
    public List<String> getTagsForTask(int taskId) {
        String query = "SELECT tag_name FROM taglist tl " +
                "JOIN tag_connector tc ON tl.tag_id = tc.tag_id " +
                "WHERE tc.task_id = ?";
        List<String> tags = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, taskId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                tags.add(resultSet.getString("tag_name"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return tags;
    }

    @Override
    public void saveTagToTask(String tagName, int taskId) {
        int tagId = getTagId(tagName);

        if (tagId != -1) {
            String query = "INSERT INTO tag_connector (tag_id, task_id) VALUES (?, ?)";

            try (Connection connection = DatabaseManager.getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {

                statement.setInt(1, tagId);
                statement.setInt(2, taskId);

                statement.executeUpdate();

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void removeAllTagsFromTask(int taskId) {
        String query = "DELETE FROM tag_connector WHERE task_id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, taskId);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
