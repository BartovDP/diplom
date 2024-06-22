package com.example;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.postgresql.PGConnection;
import org.postgresql.PGNotification;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarView;
import com.example.DAO.GroupDAO;
//import com.example.DAO.Groups;
//import com.example.DAO.Project;
import com.example.DAO.ProjectDAO;
import com.example.DAO.TagDAO;
//import com.example.DAO.Tags;
//import com.example.DAO.Task;
import com.example.DAO.TaskDAO;
import com.example.Entities.GroupDetails;
import com.example.Entities.ProjectDetails;
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
    @FXML
    private TextField projectKeyField;
    @FXML
    private Label projectKeyLabel;
    @FXML
    private CalendarView calendarView;
    @FXML
    private VBox myTasksVBox;
    @FXML
    private VBox myProjectsVBox;
    @FXML
    private VBox centerPanelVBox;
    @FXML
    private TextField projectGroupField;
    @FXML
    private ColorPicker projectColorPicker;
    @FXML
    private ChoiceBox<String> taskStatusChoiceBox;
    @FXML
    private ColorPicker taskColorPicker;
    @FXML
    private ChoiceBox<String> editTaskStatusChoiceBox;
    @FXML
    private ColorPicker editTaskColorPicker;
    @FXML
    private VBox taskListVBox;
    @FXML
    private TableView<TaskDetails> taskTableView;
    @FXML
    private TableColumn<TaskDetails, String> taskNameColumn;
    @FXML
    private TableColumn<TaskDetails, String> taskDescColumn;
    @FXML
    private TableColumn<TaskDetails, String> taskStatusColumn;
    @FXML
    private TableColumn<TaskDetails, LocalDate> taskBeginDateColumn;
    @FXML
    private TableColumn<TaskDetails, LocalDate> taskEndDateColumn;
    @FXML
    private Label usernameLabel;
    @FXML
    private VBox userTasksVBox;
    @FXML
    private ProgressBar projectProgressBar;
    @FXML
    private Label projectProgressTitle;

    private Calendar taskCalendar;
    private VBox currentEditTaskBox;
    //private VBox currentTaskGroupVBox;
    private static String currentTaskName;
    //private static String currentProjectName;
    private static int currentProjectId; // ID выбранного проекта
    private static Thread listeningThread;
    private static Connection connection;
    private static String username; // Имя пользователя, взятое из интерфейса входа
    private Set<String> currentTaskTags; // Теги текущей задачи

    private ProjectDAO projectdao;
    private GroupDAO groupdao;
    private TagDAO tagdao;
    private TaskDAO taskdao;

    @FXML
    private void initialize() {
        projectdao = DatabaseManager.getProjectDAO();
        groupdao = DatabaseManager.getGroupDAO();
        tagdao = DatabaseManager.getTagDAO();
        taskdao = DatabaseManager.getTaskDAO();

        taskNameColumn.setCellValueFactory(new PropertyValueFactory<>("taskName"));
        taskDescColumn.setCellValueFactory(new PropertyValueFactory<>("taskDescription"));
        taskStatusColumn.setCellValueFactory(new PropertyValueFactory<>("taskStatus"));
        taskBeginDateColumn.setCellValueFactory(new PropertyValueFactory<>("beginningDate"));
        taskEndDateColumn.setCellValueFactory(new PropertyValueFactory<>("endingDate"));

        taskTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                handleTableRowClick();
            }
        });

        projectTemplateComboBox.setItems(FXCollections.observableArrayList("Scrum", "Canban"));
        taskStatusChoiceBox.setItems(FXCollections.observableArrayList("Planned", "In Progress", "On Confirmation", "Done", "Canceled"));
        taskStatusChoiceBox.setValue("Planned");
        editTaskStatusChoiceBox.setItems(FXCollections.observableArrayList("Planned", "In Progress", "On Confirmation", "Done"));

        username = LoginController.getUsername();
        usernameLabel.setText(username);
        loadProjectsForUser(username);
        loadProjectGroupsForUser(username);
        loadTasksForUser();

        loadTags();

        taskCalendar = new Calendar("Tasks");
        taskCalendar.setReadOnly(true);
        CalendarSource calendarSource = new CalendarSource("My Calendars");
        calendarSource.getCalendars().add(taskCalendar);

        calendarView.getCalendarSources().add(calendarSource);
        calendarView.showMonthPage();

        calendarView.setShowAddCalendarButton(false);
        calendarView.setShowPrintButton(false);
        calendarView.setShowSearchField(false);
        calendarView.setShowPageSwitcher(false);
        calendarView.setShowSourceTrayButton(false);
        calendarView.setShowAddCalendarButton(false);

    }

    @FXML
    private void handleChangeUserClick() {
        // Логика для изменения пользователя
    }

    @FXML
    private void handleCopyProjectKey(MouseEvent event) {
        String projectKey = projectKeyLabel.getText();
        if (projectKey != null && !projectKey.isEmpty()) {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(projectKey);
            clipboard.setContent(content);
            System.out.println("Project key copied to clipboard: " + projectKey);
        }
    }

    @FXML
private void handleTaskClick(MouseEvent event) {
    VBox clickedTaskBox = (VBox) event.getSource();
    HBox taskLabelContainer = (HBox) clickedTaskBox.getChildren().get(0);
    Label taskLabel = (Label) taskLabelContainer.getChildren().get(0);
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
        String projectGroup = projectGroupField.getText();
        String projectColor = projectColorPicker.getValue().toString();

        if (projectName != null && !projectName.isEmpty()) {
            int projectId = projectdao.createProject(projectName, projectDescription, LoginController.getUserId(), projectTemplate, projectGroup, projectColor);

            if ("Scrum".equals(projectTemplate)) {
                groupdao.addTemplateScrum(projectId);
            } else if ("Canban".equals(projectTemplate)) {
                groupdao.addTemplateCanban(projectId);
            }

            //loadProjectsForUser(username);
            loadProjectGroupsForUser(username);
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
    private void handleHomeClick() {
        setViewVisibility(homeViewVBox);
    }

    @FXML
    private void handleSaveTask(boolean isEditMode) {
        String taskName, taskDescription, responsible, taskStatus;
        LocalDate beginningDate, endingDate;
        String taskColor;
        FlowPane tagsPane;

        if (isEditMode) {
            taskName = editTaskNameField.getText();
            taskDescription = editTaskDescriptionArea.getText();
            beginningDate = editBeginningDatePicker.getValue();
            endingDate = editEndingDatePicker.getValue();
            responsible = editResponsibleComboBox.getValue();
            taskStatus = editTaskStatusChoiceBox.getValue();
            taskColor = editTaskColorPicker.getValue().toString();
            tagsPane = editTaskTagsPane;
        } else {
            taskName = taskNameField.getText();
            taskDescription = taskDescriptionArea.getText();
            beginningDate = beginningDatePicker.getValue();
            endingDate = endingDatePicker.getValue();
            responsible = responsibleComboBox.getValue();
            taskStatus = taskStatusChoiceBox.getValue();
            taskColor = taskColorPicker.getValue().toString();
            tagsPane = taskTagsPane;
        }

        int userId = LoginController.getUserId(responsible);

        if (isEditMode) {
            taskdao.updateTask(currentTaskName, taskName, taskDescription, beginningDate, endingDate, taskStatus, taskColor);
            updateTagsForTask(taskName, tagsPane);
            taskdao.updateResponsibleUser(userId, userId);
        } else {
            int taskId = taskdao.saveTask(currentProjectId, taskName, taskDescription, beginningDate, endingDate, taskStatus, taskColor);
            saveTagsToTask(tagsPane, taskId);
            taskdao.saveResponsibleUser(taskId, userId);
        }

        // Reload task groups for the project to reflect changes in the UI
        projectBoardHBox.getChildren().clear();
        loadTaskGroupsForProject(currentProjectId);

        closeRightPanel();
    }


    @FXML
    private void handleJoinProject() {
        String projectKey = projectKeyField.getText();
        if (projectKey != null && !projectKey.isEmpty()) {
            int projectId = projectdao.getProjectIdByKey(projectKey);
            int userId = LoginController.getUserId();
            if (projectId != -1 && userId != -1) {
                projectdao.linkProjectToUser(projectId, userId);
                loadProjectsForUser(username);
                projectKeyField.clear();
                setViewVisibility(homeViewVBox);
            } else {
                // Вывести сообщение об ошибке, если проект не найден или id пользователя не определен
                System.out.println("Project not found or user ID is not defined.");
            }
        } else {
            // Вывести сообщение об ошибке, если ключ проекта не введен
            System.out.println("Project key is required.");
        }
    }

    @FXML
    private void handleDeleteTask() {
        taskdao.deleteTask(currentTaskName);
        projectBoardHBox.getChildren().clear();
        loadTaskGroupsForProject(currentProjectId);
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
        projectdao.deleteProject(currentProjectId);
        //loadProjectsForUser(username);
        loadProjectGroupsForUser(username);
        setViewVisibility(homeViewVBox);
    }

    @FXML
    private void handleCancel() {
        closeRightPanel();
    }

    @FXML
    private void handleCancelProject() {
        setViewVisibility(homeViewVBox);
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

    private void addTaskToGroup(VBox taskGroup, TaskDetails taskDetails, List<String> tags) {
        VBox taskBox = new VBox();
        taskBox.setSpacing(5); 
        taskBox.getStyleClass().add("task-box");

        taskBox.setOnMouseClicked(this::handleTaskClick);

        HBox taskLabelContainer = new HBox();
        taskLabelContainer.setSpacing(10);

        Label taskLabel = new Label(taskDetails.getTaskName());
        taskLabel.getStyleClass().add("task-label");

        Label taskStatus = new Label();
        loadTaskStatus(taskStatus, taskDetails.getTaskStatus(), taskDetails.getEndingDate());

        taskLabelContainer.getChildren().addAll(taskLabel, taskStatus);

        FlowPane tagsPane = new FlowPane();
        tagsPane.getStyleClass().add("tags-pane");

        // Render tags for the task
        renderTaskTags(tagsPane, tags);

        // Format dates
        String formattedBeginDate = taskDetails.getBeginningDate().format(DateTimeFormatter.ofPattern("dd.MM.yy"));
        String formattedEndDate = taskDetails.getEndingDate().format(DateTimeFormatter.ofPattern("dd.MM.yy"));
        Label dateLabel = new Label(formattedBeginDate + " - " + formattedEndDate);
        dateLabel.getStyleClass().add("date-label");
        dateLabel.setAlignment(Pos.CENTER_RIGHT);
        HBox dateContainer = new HBox(dateLabel);
        dateContainer.setAlignment(Pos.CENTER_RIGHT);

        taskBox.getChildren().addAll(taskLabelContainer, dateContainer); //tagsPane

        VBox coloredContainer = new VBox(taskBox);
        coloredContainer.setStyle("-fx-background-color: " + taskDetails.getTaskColor() + ";");
        coloredContainer.getStyleClass().add("colored-container");
        coloredContainer.setPadding(new Insets(5));  // Add some padding if necessary

        taskGroup.getChildren().add(coloredContainer);
    }

    
    
    private void loadTaskStatus(Label taskStatusLabel, String status, LocalDate endDate) {
        LocalDate currentDate = LocalDate.now();
        taskStatusLabel.getStyleClass().clear(); // Очистить предыдущие классы стилей
    
        if ("done".equalsIgnoreCase(status)) {
            taskStatusLabel.setText("✔ Done");
            taskStatusLabel.getStyleClass().add("status-done");
        } else if ("on confirmation".equalsIgnoreCase(status)) {
            taskStatusLabel.setText("On Confirmation");
            taskStatusLabel.getStyleClass().add("status-on-confirmation");
        } else if ("canceled".equalsIgnoreCase(status)) {
            taskStatusLabel.setText("✕ Canceled");
            taskStatusLabel.getStyleClass().add("status-canceled");
        } else if (currentDate.isAfter(endDate)) {
            taskStatusLabel.setText("🕑 Date Expired");
            taskStatusLabel.getStyleClass().add("status-expired");
        }
    }
    
    
    

    private void renderTaskTags(FlowPane tagsPane, List<String> tags) {
        tagsPane.getChildren().clear(); // Очистить существующие теги
        for (String tag : tags) {
            Label tagLabel = new Label(tag);
            tagLabel.getStyleClass().add("tags-pane-label");
            HBox tagPane = new HBox();
            tagPane.getStyleClass().add("tags-pane-label-back");
            Label colorLabel = new Label("❙");
            colorLabel.setStyle("-fx-text-fill: " + ColorUtils.convertDbColorToCss(tagdao.getTagColor(tag) + ";"));
            tagPane.getChildren().addAll(colorLabel, tagLabel);
            tagsPane.getChildren().add(tagPane);
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
            tagdao.saveTagToTask(tagName, taskId);
        }
    }

    private void updateTagsForTask(String taskName, FlowPane tagsPane) {
        int taskId = taskdao.getTaskId(taskName);
    
        if (taskId != -1) {
            tagdao.removeAllTagsFromTask(taskId);
            saveTagsToTask(tagsPane, taskId);
        }
    }    

    private void fillEditTaskFields(String taskName) {
        TaskDetails taskDetails = taskdao.getTaskDetails(taskName);
    
        if (taskDetails != null) {
            editTaskNameField.setText(taskDetails.getTaskName());
            editTaskDescriptionArea.setText(taskDetails.getTaskDescription());
            editBeginningDatePicker.setValue(taskDetails.getBeginningDate());
            editEndingDatePicker.setValue(taskDetails.getEndingDate());
            editTaskStatusChoiceBox.setValue(taskDetails.getTaskStatus());
            editTaskColorPicker.setValue(Color.valueOf(taskDetails.getTaskColor()));
        }
    }
        

    public void loadProjectsForUser(String username) {
        List<ProjectDetails> projects = projectdao.getProjectsForUser(username);
        myProjectsVBox.getChildren().clear();

        for (ProjectDetails project : projects) {
            int projectId = project.getProjectId();
            String projectName = project.getProjectName();
            String projectColor = project.getProjectColor();

            Label projectLabel = new Label(projectName);
            projectLabel.getStyleClass().add("home-label");
            projectLabel.setOnMouseClicked(event -> handleProjectClick(projectId, projectName));
            HBox projMarkerPane = new HBox();
            Label projMarker = new Label("⚫");
            projMarker.setStyle("-fx-text-fill: " + projectColor + ";");
            projMarkerPane.getChildren().add(projMarker);
            projMarkerPane.getChildren().add(projectLabel);

            myProjectsVBox.getChildren().add(projMarkerPane);
        }
    }

    public void loadProjectGroupsForUser(String username) {
        List<String> projectGroups = projectdao.getUserProjectGroups(username);
    
        projectsVBox.getChildren().clear();

        Label createNewLabel = new Label("Create new project");
        createNewLabel.getStyleClass().add("menu-label3");
        createNewLabel.setOnMouseClicked(event -> setViewVisibility(createProjectViewVBox));
        projectsVBox.getChildren().add(createNewLabel);
    
        for (String group : projectGroups) {
            VBox groupVBox = new VBox();
            groupVBox.getStyleClass().add("menu-box");
    
            Label groupLabel = new Label(group);
            groupLabel.getStyleClass().add("menu-label2");
            groupVBox.getChildren().add(groupLabel);
    
            List<ProjectDetails> projects = projectdao.getProjectsForUserByGroup(username, group);
    
            for (ProjectDetails project : projects) {
                int projectId = project.getProjectId();
                String projectName = project.getProjectName();
                String projectColor = ColorUtils.convertDbColorToCss(project.getProjectColor());
                Label projectLabel = new Label(projectName);
                HBox projMarkerPane = new HBox();
                Label projMarker = new Label("⚫");
                projMarker.setStyle("-fx-text-fill: " + projectColor + ";");
                projMarker.setMinWidth(15);
                projectLabel.getStyleClass().add("menu-label3");
                projectLabel.setWrapText(true);
                projectLabel.setOnMouseClicked(event -> handleProjectClick(projectId, projectName));
                projMarkerPane.getChildren().addAll(projMarker, projectLabel);
                groupVBox.getChildren().add(projMarkerPane);
            }
    
            projectsVBox.getChildren().add(groupVBox);
        }
    }

    private void loadResponsibleUsers(int projectId) {
        List<String> users = projectdao.getUsersWithAccess(projectId);
        responsibleComboBox.setItems(FXCollections.observableArrayList(users));
        editResponsibleComboBox.setItems(FXCollections.observableArrayList(users));
    }

    private void handleProjectClick(int projectId, String projectName) {
        currentProjectId = projectId;
        //currentProjectName = projectName;
        loadResponsibleUsers(projectId);
        loadTaskGroupsForProject(projectId);
        loadProjectOverview(projectId);
        setViewVisibility(projectViewVBox);
        stopListening();
        startListening(projectId);
        loadTasksForCalendar(projectId);
        loadTasksForProject(currentProjectId);
    }

    @FXML
    private void handleTableRowClick() {
        TaskDetails selectedTask = taskTableView.getSelectionModel().getSelectedItem();
        if (selectedTask != null) {
            fillEditTaskFields(selectedTask.getTaskName());
            loadTaskTags(selectedTask.getTaskName(), editTaskTagsPane, editAvailableTagsComboBox);
            showRightPanel("editTask");
        }
    }


    
    private void loadTasksForCalendar(int projectId) {
        List<TaskDetails> tasks = taskdao.getTasksForProject(projectId);
        loadTasksIntoCalendar(tasks);
    }

    private void loadTasksForProject(int projectId) {
        List<TaskDetails> tasks = taskdao.getTasksForProject(projectId);
        ObservableList<TaskDetails> taskList = FXCollections.observableArrayList(tasks);
        taskTableView.setItems(taskList);
    }

    public void loadTasksIntoCalendar(List<TaskDetails> tasks) {
        taskCalendar.clear();
        for (TaskDetails task : tasks) {
            Entry<String> entry = new Entry<>(task.getTaskName());
            entry.setInterval(task.getBeginningDate(), task.getEndingDate());
            entry.setFullDay(true);
            taskCalendar.addEntry(entry);
        }
    }

    private void loadTasksForUser() {
        int userId = LoginController.getUserId();
        List<TaskDetails> userTasks = taskdao.getTasksForUser(userId);
    
        myTasksVBox.getChildren().clear();
    
        for (TaskDetails task : userTasks) {
            Label taskLabel = new Label(task.getTaskName());
            taskLabel.getStyleClass().add("home-label");
            myTasksVBox.getChildren().add(taskLabel);
        }
    }
    

    @FXML
    private void handleCreateProjectClick(MouseEvent event) {
        setViewVisibility(createProjectViewVBox);
    }

    private void loadTaskGroupsForProject(int projectId) {
        List<GroupDetails> groups = groupdao.getGroupsForProject(projectId);
        projectBoardHBox.getChildren().clear();
    
        for (GroupDetails group : groups) {
            int groupId = group.getGroupId();
            String groupName = group.getGroupName();
            int tagId = group.getTagId();
            String tagColor = ColorUtils.convertDbColorToCss(tagdao.getTagColor(tagdao.getTagNameById(tagId)));
    
            VBox taskGroup = new VBox();
            taskGroup.getStyleClass().add("task-group");
    
            Label groupLabel = new Label(groupName);
            groupLabel.getStyleClass().add("task-group-title");
    
            VBox labelBox = new VBox(groupLabel);
            labelBox.getStyleClass().add("colored-container2");
            labelBox.setStyle("-fx-background-color: " + tagColor + ";");
    
            Label addTaskLabel = new Label("Add task");
            addTaskLabel.setOnMouseClicked(event -> handleAddTask(groupId, taskGroup, tagId));
            addTaskLabel.getStyleClass().add("add-task-label");
    
            VBox tasksVBox = new VBox();
            tasksVBox.getStyleClass().add("task-group-tasklist");
            tasksVBox.setId("tasksVBox" + groupId);
    
            taskGroup.getChildren().addAll(labelBox, tasksVBox);
            projectBoardHBox.getChildren().add(taskGroup);
    
            // Load tasks for this group
            loadTasksForGroup(currentProjectId, tagId, groupId, tasksVBox);
            tasksVBox.getChildren().add(addTaskLabel);
        }
    }
     

    private void loadTasksForGroup(int projId, int tagId, int groupId, VBox tasksVBox) {
        List<TaskDetails> tasks = taskdao.getTasksForGroup(projId, tagId, groupId);
        
        for (TaskDetails task : tasks) {
            List<String> taskTags = tagdao.getTagsForTask(taskdao.getTaskId(task.getTaskName()));
            addTaskToGroup(tasksVBox, task, taskTags);
        }
    }

    private void loadProjectOverview(int projectId) {
        ProjectDetails projectDetails = projectdao.getProjectDetails(projectId);
    
        if (projectDetails != null) {
            projectNameLabel.setText(projectDetails.getProjectName());
            projectDescriptionLabel.setText(projectDetails.getProjectDescription());
            projectTemplateLabel.setText(projectDetails.getProjectTemplate());
            projectKeyLabel.setText(projectDetails.getShareKey());
    
            List<String> users = projectdao.getUsersWithAccess(projectId);
            userAccessVBox.getChildren().clear();
            for (String user : users) {
                userAccessVBox.getChildren().add(new Label(user));
            }

            // Calculate progress and set progress bar value
            int totalTasks = projectdao.getTaskCountForProject(projectId);
            int completedTasks = projectdao.getCompletedTaskCountForProject(projectId);
            double progress = totalTasks == 0 ? 0 : (double) completedTasks / totalTasks;
            projectProgressBar.setProgress(progress);
            projectProgressTitle.setText("Tasks completed: " + completedTasks + "/" + totalTasks);
        }
    }
        

    private void handleAddTask(int groupId, VBox taskGroup, int tagId) {
        //currentTaskGroupVBox = taskGroup;
        setTagForCurrentGroup(tagId);
        loadAvailableTags(availableTagsComboBox);
        showRightPanel("task");
    }    

    private void loadTags() {
        List<String> tags = tagdao.getAllTags();
        availableTagsComboBox.getItems().addAll(tags);
        editAvailableTagsComboBox.getItems().addAll(tags);
    }    

    private void loadAvailableTags(ComboBox<String> comboBox) {
        comboBox.getItems().clear();
        List<String> tags = tagdao.getAllTags();
        for (String tagName : tags) {
            if (!currentTaskTags.contains(tagName)) {
                comboBox.getItems().add(tagName);
            }
        }
    }

    private void setTagForCurrentGroup(int tagId) {
        String tagName = tagdao.getTagNameById(tagId);
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
        int taskId = taskdao.getTaskId(taskName);
        
        if (taskId != -1) {
            List<String> tags = tagdao.getTagsForTask(taskId);
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
        if (visibleView != projectViewVBox) {
            centerPanelVBox.getStyleClass().clear();
            centerPanelVBox.getStyleClass().add("center-padding");
        }
        else {
            centerPanelVBox.getStyleClass().clear();
            centerPanelVBox.getStyleClass().add("center-panel");
        }
        closeRightPanel();
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
                                        //loadProjectsForUser(username);
                                        loadProjectGroupsForUser(username);
                                        loadTaskGroupsForProject(currentProjectId);
                                        loadProjectOverview(currentProjectId);
                                    });
                                }
                            }
                        }
                        Thread.sleep(2000);
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