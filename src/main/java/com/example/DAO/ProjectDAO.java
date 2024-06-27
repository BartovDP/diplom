package com.example.DAO;

import com.example.Entities.ProjectDetails;
import java.util.List;

public interface ProjectDAO {
    List<ProjectDetails> getProjectsForUser(String username);
    int createProject(String projectName, String projectDescription, 
                        int userId, String template, String projectGroup,
                        String projectColor);
    void linkProjectToUser(int projectId, int userId);
    void deleteProject(int projectId);
    ProjectDetails getProjectDetails(int projectId);
    List<String> getUsersWithAccess(int projectId);
    String getShareKey(int projectId);
    int getProjectIdByKey(String shareKey);
    List<String> getUserProjectGroups(String username);
    List<ProjectDetails> getProjectsForUserByGroup(String username, String group);
    int getTaskCountForProject(int projectId);
    int getCompletedTaskCountForProject(int projectId);
}


