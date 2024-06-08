package com.example.Entities;

public class ProjectDetails {
    private int projectId;
    private String projectName;
    private String projectDescription;

    public ProjectDetails(int projectId, String projectName, String projectDescription) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.projectDescription = projectDescription;
    }

    public int getProjectId() {
        return projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getProjectDescription() {
        return projectDescription;
    }
}
