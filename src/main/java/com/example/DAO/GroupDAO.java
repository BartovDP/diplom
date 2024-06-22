package com.example.DAO;

import com.example.Entities.GroupDetails;
import java.util.List;

public interface GroupDAO {
    List<GroupDetails> getGroupsForProject(int projectId);
    void createGroup(int projectId, int position, String groupName, int tagId);
    void addTemplateOne(int projectId);
    void addTemplateTwo(int projectId);
    void addTemplateScrum(int projectId);
    void addTemplateCanban(int projectId);
}
