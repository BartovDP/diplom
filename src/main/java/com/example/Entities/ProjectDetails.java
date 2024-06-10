package com.example.Entities;

public class ProjectDetails {
    private int projectId;
    private String projectName;
    private String projectDescription;
    private String projectTemplate;
    private String shareKey;

    public ProjectDetails(int projectId, String projectName, String projectDescription, String projectTemplate, String shareKey) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.projectDescription = projectDescription;
        this.projectTemplate = projectTemplate;
        this.shareKey = shareKey;
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

    public String getProjectTemplate() {
        return projectTemplate;
    }

    public String getShareKey() {
        return shareKey;
    }
}