package com.example.Entities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.example.DatabaseManager;

public class Groups {

    public static List<GroupDetails> getGroupsForProject(int projectId) {
        String query = "SELECT g.group_id, g.position, g.group_name, g.tag_id " +
                       "FROM grouplist g " +
                       "JOIN projects p ON g.proj_id = p.proj_id " +
                       "WHERE p.proj_id = ? " +
                       "ORDER BY g.position";
        List<GroupDetails> groups = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, projectId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int groupId = resultSet.getInt("group_id");
                int position = resultSet.getInt("position");
                String groupName = resultSet.getString("group_name");
                int tagId = resultSet.getInt("tag_id");
                groups.add(new GroupDetails(groupId, position, groupName, tagId));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return groups;
    }

    public static void createGroup(int projectId, int position, String groupName, int tagId) {
        String query = "INSERT INTO grouplist (proj_id, position, group_name, tag_id) VALUES (?, ?, ?, ?)";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, projectId);
            statement.setInt(2, position);
            statement.setString(3, groupName);
            statement.setInt(4, tagId);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
