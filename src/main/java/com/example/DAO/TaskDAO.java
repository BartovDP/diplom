package com.example.DAO;

import com.example.Entities.TaskDetails;
import java.time.LocalDate;
import java.util.List;

public interface TaskDAO {
    int saveTask(int projId, String taskName, String taskDesc, LocalDate taskBeg, LocalDate taskEnd, String taskStatus, String taskColor);
    void updateTask(String currentTaskName, String newTaskName, String taskDesc, LocalDate taskBeg, LocalDate taskEnd, String taskStatus, String taskColor);
    void deleteTask(String taskName);
    TaskDetails getTaskDetails(String taskName);
    int getTaskId(String taskName);
    void saveResponsibleUser(int taskId, int userId);
    void updateResponsibleUser(int taskId, int userId);
    List<TaskDetails> getTasksForProject(int projectId);
    List<TaskDetails> getTasksForUser(int userId);
    List<TaskDetails> getTasksForGroup(int projId, int tagId, int groupId);
}
