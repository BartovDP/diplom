package com.example.Entities;

import java.time.LocalDate;

public class TaskDetails {
    private String taskName;
    private String taskDescription;
    private LocalDate beginningDate;
    private LocalDate endingDate;

    public TaskDetails(String taskName, String taskDescription, LocalDate beginningDate, LocalDate endingDate) {
        this.taskName = taskName;
        this.taskDescription = taskDescription;
        this.beginningDate = beginningDate;
        this.endingDate = endingDate;
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
}
