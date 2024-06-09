package com.example;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.postgresql.PGConnection;
import org.postgresql.PGNotification;

import com.example.Entities.Group;
import com.example.Entities.Project;
import com.example.Entities.ProjectDetails;
import com.example.Entities.Tags;
import com.example.Entities.Task;
import com.example.Entities.TaskDetails;

public class MainController {
    @FXML
    private VBox rightPanelVBox;
    @FXML
    private VBox projectCreateVBox;
    @FXML
    private VBox taskCreateVBox;
    @FXML
    private VBox taskEditVBox;
    @FXML
    private TextArea projectDescriptionArea;
    @FXML
    private TextField taskNameField;
    @FXML
    private TextArea taskDescriptionArea;
    @FXML
    private DatePicker beginningDatePicker;
    @FXML
    private DatePicker endingDatePicker;
    @FXML
    private ComboBox<String> responsibleComboBox;
    @FXML
    private ComboBox<String> availableTagsComboBox;
    @FXML
    private FlowPane taskTagsPane;
    @FXML
    private TextField editTaskNameField;
    @FXML
    private TextArea editTaskDescriptionArea;
    @FXML
    private DatePicker editBeginningDatePicker;
    @FXML
    private DatePicker editEndingDatePicker;
    @FXML
    private ComboBox<String> editResponsibleComboBox;
    @FXML
    private ComboBox<String> editAvailableTagsComboBox;
    @FXML
    private FlowPane editTaskTagsPane;
    @FXML
    private VBox projectsVBox;
    @FXML
    private HBox projectBoardHBox;
    @FXML
    private VBox homeViewVBox;
    @FXML
    private VBox projectViewVBox;
    @FXML
    private VBox createProjectViewVBox;
    @FXML
    private TextField projectNameField;
    @FXML
    private TextField newProjectNameField;
    @FXML
    private TextArea newProjectDescriptionArea;
    @FXML
    private ComboBox<String> projectTemplateComboBox;
    @FXML
    private Label projectNameLabel;
    @FXML
    private Label projectDescriptionLabel;
    @FXML
    private Label projectTemplateLabel;
    @FXML
    private VBox userAccessVBox;


    private VBox currentEditTaskBox;
    private VBox currentTaskGroupVBox;
    private static String currentTaskName;
    private static String currentProjectName;
    private static int currentProjectId; // ID выбранного проекта
    private static Thread listeningThread;
    private static Connection connection;
    private static String username; // Имя пользователя, взятое из интерфейса входа
    private Set<String> currentTaskTags; // Теги текущей задачи

    @FXML
    private void initialize() {
        responsibleComboBox.setItems(FXCollections.observableArrayList("User 1", "User 2", "User 3"));
        editResponsibleComboBox.setItems(FXCollections.observableArrayList("User 1", "User 2", "User 3"));
        projectTemplateComboBox.setItems(FXCollections.observableArrayList("Template One", "Template Two"));

        username = LoginController.getUsername();
        loadProjectsForUser(username);

        loadTags();
    }

    @FXML
    private void handleTaskClick(MouseEvent event) {
        currentEditTaskBox = (VBox) event.getSource();
        Label taskLabel = (Label) currentEditTaskBox.getChildren().get(0);
        currentTaskName = taskLabel.getText();

        fillEditTaskFields(currentTaskName);
        loadTaskTags(currentTaskName, editTaskTagsPane, editAvailableTagsComboBox);

        showRightPanel("editTask");
    }

    @FXML
    private void handleCreateProject() {
        String projectName = newProjectNameField.getText();
        String projectDescription = newProjectDescriptionArea.getText();
        String projectTemplate = projectTemplateComboBox.getValue();
    
        if (projectName != null && !projectName.isEmpty()) {
            int projectId = Project.createProject(projectName, projectDescription, LoginController.getUserId());

            if ("Template One".equals(projectTemplate)) {
                Project.addTemplateOne(projectId);
            } else if ("Template Two".equals(projectTemplate)) {
                Project.addTemplateTwo(projectId);
            }

            loadProjectsForUser(username);
            setViewVisibility(homeViewVBox);
        } else {
            // Вывести сообщение об ошибке, если имя проекта не указано
            System.out.println("Project name is required.");
        }
    }

    @FXML
    private void handleCreateTask() {
        handleSaveTask(false);
    }

    @FXML
    private void handleEditTask() {
        handleSaveTask(true);
    }


    @FXML
    private void handleSaveTask(boolean isEditMode) {
        String taskName, taskDescription, responsible;
        LocalDate beginningDate, endingDate;
        FlowPane tagsPane;

        if (isEditMode) {
            taskName = editTaskNameField.getText();
            taskDescription = editTaskDescriptionArea.getText();
            beginningDate = editBeginningDatePicker.getValue();
            endingDate = editEndingDatePicker.getValue();
            responsible = editResponsibleComboBox.getValue();
            tagsPane = editTaskTagsPane;
        } else {
            taskName = taskNameField.getText();
            taskDescription = taskDescriptionArea.getText();
            beginningDate = beginningDatePicker.getValue();
            endingDate = endingDatePicker.getValue();
            responsible = responsibleComboBox.getValue();
            tagsPane = taskTagsPane;
        }

        if (isEditMode) {
            Task.updateTask(currentTaskName, taskName, taskDescription, beginningDate, endingDate);
            updateTagsForTask(taskName, tagsPane);
        } else {
            int taskId = Task.saveTask(currentProjectId, taskName, taskDescription, beginningDate, endingDate);
            saveTagsToTask(tagsPane, taskId);
        }

        // Reload task groups for the project to reflect changes in the UI
        projectBoardHBox.getChildren().clear();
        loadTaskGroupsForProject(currentProjectName);

        closeRightPanel();
    }

    @FXML
    private void handleDeleteTask() {
        Task.deleteTask(currentTaskName);
        projectBoardHBox.getChildren().clear();
        loadTaskGroupsForProject(currentProjectName);
        closeRightPanel();
    }

    @FXML
    private void handleTagSelection(ComboBox<String> tagComboBox, FlowPane tagsPane) {
        String selectedTag = tagComboBox.getValue();
        if (selectedTag != null) {
            Label tagLabel = new Label(selectedTag);
            tagLabel.setOnMouseClicked(this::handleTagDoubleClick); // Добавляем обработчик двойного нажатия
            tagsPane.getChildren().add(tagLabel);
            tagComboBox.getItems().remove(selectedTag);
            tagComboBox.setValue(null);
        }
    }

    @FXML
    private void handleTagSelection() {
        handleTagSelection(availableTagsComboBox, taskTagsPane);
    }

    @FXML
    private void handleEditTagSelection() {
        handleTagSelection(editAvailableTagsComboBox, editTaskTagsPane);
    }


    private void handleTagDoubleClick(MouseEvent event) {
        if (event.getClickCount() == 2) {
            Label tagLabel = (Label) event.getSource();
            FlowPane tagsPane = (FlowPane) tagLabel.getParent();
            tagsPane.getChildren().remove(tagLabel);
            ComboBox<String> tagComboBox = tagsPane == taskTagsPane ? availableTagsComboBox : editAvailableTagsComboBox;
            tagComboBox.getItems().add(tagLabel.getText());
        }
    }

    @FXML
    private void handleDeleteProject() {
        Project.deleteProject(currentProjectId);
        loadProjectsForUser(username);
        setViewVisibility(homeViewVBox);
    }

    @FXML
    private void handleCancel() {
        closeRightPanel();
    }

    private void showRightPanel(String type) {
        boolean isVisible = rightPanelVBox.isVisible();
        if (!isVisible) {
            rightPanelVBox.setVisible(true);
            rightPanelVBox.setManaged(true);
        }
        switch (type) {
            case "project":
                projectCreateVBox.setVisible(true);
                projectCreateVBox.setManaged(true);
                taskCreateVBox.setVisible(false);
                taskCreateVBox.setManaged(false);
                taskEditVBox.setVisible(false);
                taskEditVBox.setManaged(false);
                break;
            case "task":
                taskCreateVBox.setVisible(true);
                taskCreateVBox.setManaged(true);
                projectCreateVBox.setVisible(false);
                projectCreateVBox.setManaged(false);
                taskEditVBox.setVisible(false);
                taskEditVBox.setManaged(false);
                break;
            case "editTask":
                taskEditVBox.setVisible(true);
                taskEditVBox.setManaged(true);
                taskCreateVBox.setVisible(false);
                taskCreateVBox.setManaged(false);
                projectCreateVBox.setVisible(false);
                projectCreateVBox.setManaged(false);
                break;
        }
    }

    private void addTaskToGroup(VBox taskGroup, String taskName, List<String> tags) {
        VBox taskBox = new VBox();
        taskBox.setSpacing(5);
        taskBox.getStyleClass().add("task-box");
    
        taskBox.setOnMouseClicked(this::handleTaskClick);
    
        Label taskLabel = new Label(taskName);
        taskLabel.getStyleClass().add("task-label");
    
        FlowPane tagsPane = new FlowPane();
        tagsPane.getStyleClass().add("tags-pane");
    
        // Отрисовать теги для задачи
        renderTaskTags(tagsPane, tags);
    
        taskBox.getChildren().addAll(taskLabel, tagsPane);
        taskGroup.getChildren().add(taskBox);
    }    

    private void renderTaskTags(FlowPane tagsPane, List<String> tags) {
        tagsPane.getChildren().clear(); // Очистить существующие теги
        for (String tag : tags) {
            Label tagLabel = new Label(tag);
            tagLabel.getStyleClass().add("tag-label");
            tagsPane.getChildren().add(tagLabel);
        }
    }    

    private void closeRightPanel() {
        rightPanelVBox.setVisible(false);
        rightPanelVBox.setManaged(false);
        clearFields();
    }

    private void clearFields() {
        projectNameField.clear();
        projectDescriptionArea.clear();
        taskNameField.clear();
        taskDescriptionArea.clear();
        beginningDatePicker.setValue(null);
        endingDatePicker.setValue(null);
        responsibleComboBox.setValue(null);
        availableTagsComboBox.setValue(null);
        taskTagsPane.getChildren().clear();
        editTaskNameField.clear();
        editTaskDescriptionArea.clear();
        editBeginningDatePicker.setValue(null);
        editEndingDatePicker.setValue(null);
        editResponsibleComboBox.setValue(null);
        editAvailableTagsComboBox.setValue(null);
        editTaskTagsPane.getChildren().clear();
    }

    private void saveTagsToTask(FlowPane tagsPane, int taskId) {
        for (String tagName : getTagsFromPane(tagsPane)) {
            Tags.saveTagToTask(tagName, taskId);
        }
    }

    private void updateTagsForTask(String taskName, FlowPane tagsPane) {
        int taskId = Task.getTaskId(taskName);
    
        if (taskId != -1) {
            Tags.removeAllTagsFromTask(taskId);
            saveTagsToTask(tagsPane, taskId);
        }
    }    

    private void fillEditTaskFields(String taskName) {
        TaskDetails taskDetails = Task.getTaskDetails(taskName);
    
        if (taskDetails != null) {
            editTaskNameField.setText(taskDetails.getTaskName());
            editTaskDescriptionArea.setText(taskDetails.getTaskDescription());
            editBeginningDatePicker.setValue(taskDetails.getBeginningDate());
            editEndingDatePicker.setValue(taskDetails.getEndingDate());
        }
    }    

    public void loadProjectsForUser(String username) {
        List<ProjectDetails> projects = Project.getProjectsForUser(username);

        projectsVBox.getChildren().clear();

        Label createNewLabel = new Label("Create New");
        createNewLabel.getStyleClass().add("project-label");
        createNewLabel.setOnMouseClicked(event -> setViewVisibility(createProjectViewVBox));
        projectsVBox.getChildren().add(createNewLabel);

        for (ProjectDetails project : projects) {
            int projectId = project.getProjectId();
            String projectName = project.getProjectName();
            String projectDescription = project.getProjectDescription();

            Label projectLabel = new Label(projectName);
            projectLabel.getStyleClass().add("project-label");
            projectLabel.setOnMouseClicked(event -> handleProjectClick(projectId, projectName));

            projectsVBox.getChildren().add(projectLabel);
        }
    }

    private void handleProjectClick(int projectId, String projectName) {
        currentProjectId = projectId;
        currentProjectName = projectName;
        loadTaskGroupsForProject(projectName);
        loadProjectOverview(projectId);
        setViewVisibility(projectViewVBox);
        stopListening();
        startListening(projectId);
    }

    private void loadTaskGroupsForProject(String projectName) {
        List<Group> groups = Tags.getGroupsForProject(projectName);
        projectBoardHBox.getChildren().clear();

        for (Group group : groups) {
            int groupId = group.getGroupId();
            String groupName = group.getGroupName();
            int tagId = group.getTagId();

            VBox taskGroup = new VBox();
            taskGroup.setMinWidth(250);
            taskGroup.setPrefWidth(250);
            taskGroup.setSpacing(10);
            taskGroup.getStyleClass().add("task-group");

            Label groupLabel = new Label(groupName);
            groupLabel.getStyleClass().add("task-group-title");

            Label addTaskLabel = new Label("Add task");
            addTaskLabel.setOnMouseClicked(event -> handleAddTask(groupId, taskGroup, tagId));
            addTaskLabel.getStyleClass().add("add-task-label");

            VBox tasksVBox = new VBox();
            tasksVBox.setSpacing(5);
            tasksVBox.setId("tasksVBox" + groupId);

            taskGroup.getChildren().addAll(groupLabel, addTaskLabel, tasksVBox);
            projectBoardHBox.getChildren().add(taskGroup);

            // Загрузка задач для данной группы
            loadTasksForGroup(currentProjectId, tagId, groupId, tasksVBox);
        }
    } 

    private void loadTasksForGroup(int projId, int tagId, int groupId, VBox tasksVBox) {
        List<String> tasks = Tags.getTasksForGroup(projId, tagId, groupId);
        for (String taskName : tasks) {
            List<String> taskTags = Tags.getTagsForTask(Task.getTaskId(taskName));
            addTaskToGroup(tasksVBox, taskName, taskTags);
        }
    }

    private void loadProjectOverview(int projectId) {
        ProjectDetails projectDetails = Project.getProjectDetails(projectId);
    
        if (projectDetails != null) {
            projectNameLabel.setText(projectDetails.getProjectName());
            projectDescriptionLabel.setText(projectDetails.getProjectDescription());
            projectTemplateLabel.setText(""); // Пока не заполняем
    
            List<String> users = Project.getUsersWithAccess(projectId);
            userAccessVBox.getChildren().clear();
            for (String user : users) {
                userAccessVBox.getChildren().add(new Label(user));
            }
        }
    }
        

    private void handleAddTask(int groupId, VBox taskGroup, int tagId) {
        currentTaskGroupVBox = taskGroup;
        setTagForCurrentGroup(tagId);
        loadAvailableTags(availableTagsComboBox);
        showRightPanel("task");
    }    

    private void loadTags() {
        List<String> tags = Tags.getAllTags();
        availableTagsComboBox.getItems().addAll(tags);
        editAvailableTagsComboBox.getItems().addAll(tags);
    }    

    private void loadAvailableTags(ComboBox<String> comboBox) {
        comboBox.getItems().clear();
        List<String> tags = Tags.getAllTags();
        for (String tagName : tags) {
            if (!currentTaskTags.contains(tagName)) {
                comboBox.getItems().add(tagName);
            }
        }
    }

    private void setTagForCurrentGroup(int tagId) {
        String tagName = Tags.getTagNameById(tagId);
        if (tagName != null) {
            taskTagsPane.getChildren().clear();
            taskTagsPane.getChildren().add(new Label(tagName));
            currentTaskTags = new HashSet<>();
            currentTaskTags.add(tagName);
        }
    }


    private Set<String> getTagsFromPane(FlowPane tagsPane) {
        Set<String> tags = new HashSet<>();
        for (javafx.scene.Node node : tagsPane.getChildren()) {
            if (node instanceof Label) {
                tags.add(((Label) node).getText());
            }
        }
        return tags;
    }

    private void loadTaskTags(String taskName, FlowPane tagsPane, ComboBox<String> availableTagsComboBox) {
        tagsPane.getChildren().clear();
        currentTaskTags = new HashSet<>();
        int taskId = Task.getTaskId(taskName);
        
        if (taskId != -1) {
            List<String> tags = Tags.getTagsForTask(taskId);
            for (String tagName : tags) {
                Label tagLabel = new Label(tagName);
                tagLabel.setOnMouseClicked(this::handleTagDoubleClick);
                tagsPane.getChildren().add(tagLabel);
                currentTaskTags.add(tagName);
            }
        }
    
        loadAvailableTags(availableTagsComboBox);
    }

    private void setViewVisibility(VBox visibleView) {
        homeViewVBox.setVisible(false);
        homeViewVBox.setManaged(false);
        projectViewVBox.setVisible(false);
        projectViewVBox.setManaged(false);
        createProjectViewVBox.setVisible(false);
        createProjectViewVBox.setManaged(false);

        visibleView.setVisible(true);
        visibleView.setManaged(true);

        if (visibleView != createProjectViewVBox) {
            newProjectNameField.clear();
            newProjectDescriptionArea.clear();
            projectTemplateComboBox.setValue(null);
        }
    }


    public void startListening(int proj_id) {
        try {
            connection = DatabaseManager.getConnection();
            PGConnection pgConnection = connection.unwrap(PGConnection.class);
            String channelName = "project_channel_" + proj_id;

            // Создание подписки на канал
            Statement stmt = connection.createStatement();
            stmt.execute("LISTEN " + channelName);
            stmt.close();

            // Запуск потока для обработки уведомлений
            listeningThread = new Thread(() -> {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        PGNotification[] notifications = pgConnection.getNotifications();
                        System.out.println("Receiving notifications for " + channelName);
                        if (notifications != null) {
                            for (PGNotification notification : notifications) {
                                if (notification.getName().equals(channelName)) {
                                    String payload = notification.getParameter();
                                    System.out.println("Received notification on " + channelName + ": " + payload);
                                    
                                    Platform.runLater(() -> {
                                        loadProjectsForUser(username);
                                        loadTaskGroupsForProject(currentProjectName);
                                        loadProjectOverview(currentProjectId);
                                    });
                                }
                            }
                        }
                        Thread.sleep(1000);
                    }
                } catch (SQLException | InterruptedException e) {
                    if (e instanceof InterruptedException) {
                        System.out.println("Listening thread interrupted");
                    } else {
                        e.printStackTrace();
                    }
                }
            });
            listeningThread.start();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void stopListening() {
        if (listeningThread != null && listeningThread.isAlive()) {
            listeningThread.interrupt();
            try {
                if (connection != null && !connection.isClosed()) {
                    Statement stmt = connection.createStatement();
                    stmt.execute("UNLISTEN *");
                    stmt.close();
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
