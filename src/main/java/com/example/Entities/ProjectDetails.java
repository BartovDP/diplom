package com.example.Entities;

public class ProjectDetails {
    private int projectId;
    private String projectName;
    private String projectDescription;
    private String projectTemplate;
    private String shareKey;
    private String projectGroup;
    private String projectColor;

    public ProjectDetails(int projectId, String projectName, String projectDescription, String projectTemplate, String shareKey, String projectGroup, String projectColor) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.projectDescription = projectDescription;
        this.projectTemplate = projectTemplate;
        this.shareKey = shareKey;
        this.projectGroup = projectGroup;
        this.projectColor = projectColor;
    }

    public ProjectDetails(int projectId, String projectName, String projectDescription, String projectTemplate, String projectGroup, String projectColor) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.projectDescription = projectDescription;
        this.projectTemplate = projectTemplate;
        this.projectGroup = projectGroup;
        this.projectColor = projectColor;
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

    public String getProjectGroup() {
        return projectGroup;
    }

    public String getProjectColor() {
        return projectColor;
    }
}
