package com.example.DAO;

import com.example.DatabaseManager;
import com.example.Entities.GroupDetails;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PostgresGroupDAO implements GroupDAO {

    @Override
    public List<GroupDetails> getGroupsForProject(int projectId) {
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

    @Override
    public void createGroup(int projectId, int position, String groupName, int tagId) {
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

    @Override
    public void addTemplateOne(int projectId) {
        createGroup(projectId, 1, "Group 1", 1);
        createGroup(projectId, 2, "Group 2", 2);
    }

    @Override
    public void addTemplateTwo(int projectId) {
        createGroup(projectId, 1, "Group 3", 3);
    }

    @Override
    public void addTemplateScrum(int projectId) {
        createGroup(projectId, 1, "Текущий спринт", 4);
        createGroup(projectId, 2, "В разработке", 6);
        createGroup(projectId, 3, "На проверке", 7);
        createGroup(projectId, 4, "Выполнено", 8);
    }

    @Override
    public void addTemplateCanban(int projectId) {
        createGroup(projectId, 1, "Запланировано", 5);
        createGroup(projectId, 2, "Проектирование", 10);
        createGroup(projectId, 3, "Разработка", 6);
        createGroup(projectId, 4, "Тестирование", 7);
        createGroup(projectId, 5, "Готово", 8);
    }
}
