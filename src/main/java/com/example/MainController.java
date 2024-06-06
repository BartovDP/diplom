package com.example;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

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
    private TextField projectNameField;

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

    private VBox currentEditTaskBox;
    private VBox currentTaskGroupVBox;
    private String currentTaskName;
    private String currentProjectName;
    private int currentProjectId; // ID выбранного проекта

    private String username; // Имя пользователя, взятое из интерфейса входа
    private Set<String> currentTaskTags; // Теги текущей задачи

    @FXML
    private void initialize() {
        // Initialize ComboBox items
        responsibleComboBox.setItems(FXCollections.observableArrayList("User 1", "User 2", "User 3"));
        editResponsibleComboBox.setItems(FXCollections.observableArrayList("User 1", "User 2", "User 3"));

        // Загрузка имени пользователя из LoginController
        username = LoginController.getUsername();
        loadProjectsForUser(username);

        // Загрузка тегов в выпадающий список
        loadTags();
    }

    @FXML
    private void handleTaskClick(MouseEvent event) { //нажатие на задачу открывает редактирование задачи
        currentEditTaskBox = (VBox) event.getSource();
        Label taskLabel = (Label) currentEditTaskBox.getChildren().get(0);
        currentTaskName = taskLabel.getText();

        // Заполняем поля данными из базы данных
        fillEditTaskFields(currentTaskName);
        loadTaskTags(currentTaskName, editTaskTagsPane, editAvailableTagsComboBox);

        showRightPanel("editTask");
    }

    @FXML
    private void handleCreateProject() { //создание
        String projectName = projectNameField.getText();
        String projectDescription = projectDescriptionArea.getText();
        System.out.println("Project Created: " + projectName + " - " + projectDescription);
        // Add logic to create a new project
        closeRightPanel();
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

        System.out.println("Task " + (isEditMode ? "Edited: " : "Created: ") + taskName + " - " + taskDescription);
        System.out.println("Beginning Date: " + beginningDate);
        System.out.println("Ending Date: " + endingDate);
        System.out.println("Responsible: " + responsible);

        if (isEditMode) {
            // Update task in database
            updateTaskInDatabase(currentTaskName, taskName, taskDescription, beginningDate, endingDate);
            // Update tags in database
            updateTagsForTask(taskName, tagsPane);
        } else {
            // Save task to database and get task_id
            int taskId = saveTaskToDatabase(currentProjectId, taskName, taskDescription, beginningDate, endingDate);
            // Save tags to task
            saveTagsToTask(tagsPane, taskId);
        }

        // Reload task groups for the project to reflect changes in the UI
        projectBoardHBox.getChildren().clear();
        loadTaskGroupsForProject(currentProjectName);

        closeRightPanel();
    }

    @FXML
    private void handleDeleteTask() {
        deleteTaskFromDatabase(currentTaskName);
        projectBoardHBox.getChildren().clear();
        loadTaskGroupsForProject(currentProjectName);
        closeRightPanel();
    }

    private void deleteTaskFromDatabase(String taskName) {
        String query = "DELETE FROM tasklist WHERE task_name = ?";

        try (Connection connection = DatabaseManager.getConnection();
            PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, taskName);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
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

    private void addTaskToGroup(VBox taskGroup, String taskName) {
        VBox taskBox = new VBox();
        taskBox.setSpacing(5);
        taskBox.getStyleClass().add("task-box");

        taskBox.setOnMouseClicked(this::handleTaskClick);

        Label taskLabel = new Label(taskName);
        taskLabel.getStyleClass().add("task-label");

        FlowPane tagsPane = new FlowPane();
        tagsPane.getStyleClass().add("tags-pane");

        taskBox.getChildren().addAll(taskLabel, tagsPane);
        taskGroup.getChildren().add(taskBox);
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

    private int saveTaskToDatabase(int projId, String taskName, String taskDesc, LocalDate taskBeg, LocalDate taskEnd) {
        String query = "INSERT INTO tasklist (proj_id, task_name, task_desc, task_beg, task_end) VALUES (?, ?, ?, ?, ?)";
        int taskId = -1;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, projId);
            statement.setString(2, taskName);
            statement.setString(3, taskDesc);
            statement.setDate(4, java.sql.Date.valueOf(taskBeg));
            statement.setDate(5, java.sql.Date.valueOf(taskEnd));

            statement.executeUpdate();

            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                taskId = generatedKeys.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return taskId;
    }

    private void saveTagsToTask(FlowPane tagsPane, int taskId) {
        for (String tagName : getTagsFromPane(tagsPane)) {
            saveTagToTask(tagName, taskId);
        }
    }

    private void saveTagToTask(String tagName, int taskId) {
        int tagId = getTagId(tagName);

        if (tagId != -1) {
            String query = "INSERT INTO tag_connector (tag_id, task_id) VALUES (?, ?)";

            try (Connection connection = DatabaseManager.getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {

                statement.setInt(1, tagId);
                statement.setInt(2, taskId);

                statement.executeUpdate();

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private int getTagId(String tagName) {
        String query = "SELECT tag_id FROM taglist WHERE tag_name = ?";
        int tagId = -1;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, tagName);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                tagId = resultSet.getInt("tag_id");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return tagId;
    }

    private void updateTaskInDatabase(String currentTaskName, String newTaskName, String taskDesc, LocalDate taskBeg, LocalDate taskEnd) {
        String query = "UPDATE tasklist SET task_name = ?, task_desc = ?, task_beg = ?, task_end = ? WHERE task_name = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, newTaskName);
            statement.setString(2, taskDesc);
            statement.setDate(3, java.sql.Date.valueOf(taskBeg));
            statement.setDate(4, java.sql.Date.valueOf(taskEnd));
            statement.setString(5, currentTaskName);

            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateTagsForTask(String taskName, FlowPane tagsPane) {
        int taskId = getTaskId(taskName);

        if (taskId != -1) {
            removeAllTagsFromTask(taskId);
            saveTagsToTask(tagsPane, taskId);
        }
    }

    private void removeAllTagsFromTask(int taskId) {
        String query = "DELETE FROM tag_connector WHERE task_id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, taskId);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int getTaskId(String taskName) {
        String query = "SELECT task_id FROM tasklist WHERE task_name = ?";
        int taskId = -1;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, taskName);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                taskId = resultSet.getInt("task_id");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return taskId;
    }

    private void fillEditTaskFields(String taskName) {
        String query = "SELECT task_name, task_desc, task_beg, task_end FROM tasklist WHERE task_name = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, taskName);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                editTaskNameField.setText(resultSet.getString("task_name"));
                editTaskDescriptionArea.setText(resultSet.getString("task_desc"));
                editBeginningDatePicker.setValue(resultSet.getDate("task_beg").toLocalDate());
                editEndingDatePicker.setValue(resultSet.getDate("task_end").toLocalDate());
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadProjectsForUser(String username) {
        String query = "SELECT projects.proj_id, projects.proj_name, projects.proj_desc " +
                "FROM projects " +
                "JOIN pu_connector ON projects.proj_id = pu_connector.proj_id " +
                "JOIN userlist ON pu_connector.user_id = userlist.user_id " +
                "WHERE userlist.user_name = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();

            projectsVBox.getChildren().clear();
            while (resultSet.next()) {
                int projectId = resultSet.getInt("proj_id");
                String projectName = resultSet.getString("proj_name");
                String projectDescription = resultSet.getString("proj_desc");

                Label projectLabel = new Label(projectName);
                projectLabel.getStyleClass().add("project-label");
                projectLabel.setOnMouseClicked(event -> handleProjectClick(projectId, projectName));

                projectsVBox.getChildren().add(projectLabel);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleProjectClick(int projectId, String projectName) {
        // Запоминаем выбранный проект
        currentProjectId = projectId;
        currentProjectName = projectName;

        // Clear the current project board
        projectBoardHBox.getChildren().clear();

        // Load task groups for the selected project
        loadTaskGroupsForProject(projectName);
    }

    private void loadTaskGroupsForProject(String projectName) {
        String query = "SELECT g.group_id, g.position, g.group_name, g.tag_id " +
                "FROM grouplist g " +
                "JOIN projects p ON g.proj_id = p.proj_id " +
                "WHERE p.proj_name = ? " +
                "ORDER BY g.position";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, projectName);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int groupId = resultSet.getInt("group_id");
                String groupName = resultSet.getString("group_name");
                int tagId = resultSet.getInt("tag_id");

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

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadTasksForGroup(int projId, int tagId, int groupId, VBox tasksVBox) {
        String query = "SELECT DISTINCT t.task_name " +
                "FROM tasklist t " +
                "JOIN tag_connector tc ON t.task_id = tc.task_id " +
                "JOIN taglist tl ON tc.tag_id = tl.tag_id " +
                "JOIN grouplist g ON tl.tag_id = g.tag_id " +
                "WHERE g.tag_id = ? AND t.proj_id = ? AND g.group_id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, tagId);
            statement.setInt(2, projId);
            statement.setInt(3, groupId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String taskName = resultSet.getString("task_name");
                addTaskToGroup(tasksVBox, taskName);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleAddTask(int groupId, VBox taskGroup, int tagId) {
        currentTaskGroupVBox = taskGroup;
        setTagForCurrentGroup(tagId);
        loadAvailableTags(availableTagsComboBox);
        showRightPanel("task");
    }

    private void loadTags() {
        String query = "SELECT tag_name FROM taglist";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String tagName = resultSet.getString("tag_name");
                availableTagsComboBox.getItems().add(tagName);
                editAvailableTagsComboBox.getItems().add(tagName);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadAvailableTags(ComboBox<String> comboBox) {
        comboBox.getItems().clear();
        String query = "SELECT tag_name FROM taglist";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String tagName = resultSet.getString("tag_name");
                if (!currentTaskTags.contains(tagName)) {
                    comboBox.getItems().add(tagName);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setTagForCurrentGroup(int tagId) {
        String query = "SELECT tag_name FROM taglist WHERE tag_id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, tagId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String tagName = resultSet.getString("tag_name");
                taskTagsPane.getChildren().clear();
                taskTagsPane.getChildren().add(new Label(tagName));
                currentTaskTags = new HashSet<>();
                currentTaskTags.add(tagName);
            }

        } catch (SQLException e) {
            e.printStackTrace();
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
        String query = "SELECT tag_name FROM taglist tl " +
                "JOIN tag_connector tc ON tl.tag_id = tc.tag_id " +
                "JOIN tasklist t ON tc.task_id = t.task_id " +
                "WHERE t.task_name = ?";
    
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
    
            statement.setString(1, taskName);
            ResultSet resultSet = statement.executeQuery();
    
            while (resultSet.next()) {
                String tagName = resultSet.getString("tag_name");
                Label tagLabel = new Label(tagName);
                tagLabel.setOnMouseClicked(this::handleTagDoubleClick); // Добавляем обработчик двойного нажатия
                tagsPane.getChildren().add(tagLabel);
                currentTaskTags.add(tagName);
            }
    
            loadAvailableTags(availableTagsComboBox);
    
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }    
}
