package com.example.Entities;

import java.time.LocalDate;

public class TaskDetails {
    private String taskName;
    private String taskDescription;
    private LocalDate beginningDate;
    private LocalDate endingDate;
    private String taskStatus;
    private String taskColor;

    public TaskDetails(String taskName, String taskDescription, LocalDate beginningDate, LocalDate endingDate, String taskStatus, String taskColor) {
        this.taskName = taskName;
        this.taskDescription = taskDescription;
        this.beginningDate = beginningDate;
        this.endingDate = endingDate;
        this.taskStatus = taskStatus;
        this.taskColor = taskColor;
    }

    public String getTaskName() {
        return taskName;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public LocalDate getBeginningDate() {
        return beginningDate;
    }

    public LocalDate getEndingDate() {
        return endingDate;
    }

    public String getTaskStatus() {
        return taskStatus;
    }

    public String getTaskColor() {
        return taskColor;
    }
}
