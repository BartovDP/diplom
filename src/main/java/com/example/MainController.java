package com.example;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import java.time.LocalDate;

public class MainController {
    @FXML
    private VBox rightPanelVBox;

    @FXML
    private VBox tasksVBox1;

    @FXML
    private VBox tasksVBox2;

    @FXML
    private VBox tasksVBox3;

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
    private TextField editTaskNameField;

    @FXML
    private TextArea editTaskDescriptionArea;

    @FXML
    private DatePicker editBeginningDatePicker;

    @FXML
    private DatePicker editEndingDatePicker;

    @FXML
    private ComboBox<String> editResponsibleComboBox;

    //private VBox currentTaskVBox;
    private VBox currentEditTaskBox;
    private VBox currentTaskGroupVBox;

    @FXML
    private void initialize() {
        // Initialize ComboBox items
        responsibleComboBox.setItems(FXCollections.observableArrayList("User 1", "User 2", "User 3"));
        editResponsibleComboBox.setItems(FXCollections.observableArrayList("User 1", "User 2", "User 3"));
    }

    @FXML
    private void handleProjectsClick(MouseEvent event) {
        showRightPanel("project");
    }

    @FXML
    private void handleMenu1Action() {
        showRightPanel("task");
    }

    @FXML
    private void handleMenu2Action() {
        addTaskToFirstColumn();
    }

    @FXML
    private void handleTaskClick(MouseEvent event) {
        currentEditTaskBox = (VBox) event.getSource();
        Label taskLabel = (Label) currentEditTaskBox.getChildren().get(0);
        String taskName = taskLabel.getText();

        // Assuming the task data can be extracted from the currentEditTaskBox
        // Fill edit fields with task data
        editTaskNameField.setText(taskName);
        // Fill other fields with task data as needed

        showRightPanel("editTask");
    }

    @FXML
    private void handleAddTask1() {
        currentTaskGroupVBox = tasksVBox1;
        showRightPanel("task");
    }

    @FXML
    private void handleAddTask2() {
        currentTaskGroupVBox = tasksVBox2;
        showRightPanel("task");
    }

    @FXML
    private void handleAddTask3() {
        currentTaskGroupVBox = tasksVBox3;
        showRightPanel("task");
    }

    @FXML
    private void handleCreateProject() {
        String projectName = projectNameField.getText();
        String projectDescription = projectDescriptionArea.getText();
        System.out.println("Project Created: " + projectName + " - " + projectDescription);
        // Add logic to create a new project
        closeRightPanel();
    }

    @FXML
    private void handleCreateTask() {
        String taskName = taskNameField.getText();
        String taskDescription = taskDescriptionArea.getText();
        LocalDate beginningDate = beginningDatePicker.getValue();
        LocalDate endingDate = endingDatePicker.getValue();
        String responsible = responsibleComboBox.getValue();

        System.out.println("Task Created: " + taskName + " - " + taskDescription);
        System.out.println("Beginning Date: " + beginningDate);
        System.out.println("Ending Date: " + endingDate);
        System.out.println("Responsible: " + responsible);

        addTaskToGroup(currentTaskGroupVBox, taskName);
        closeRightPanel();
    }

    @FXML
    private void handleEditTask() {
        String taskName = editTaskNameField.getText();
        String taskDescription = editTaskDescriptionArea.getText();
        LocalDate beginningDate = editBeginningDatePicker.getValue();
        LocalDate endingDate = editEndingDatePicker.getValue();
        String responsible = editResponsibleComboBox.getValue();

        System.out.println("Task Edited: " + taskName + " - " + taskDescription);
        System.out.println("Beginning Date: " + beginningDate);
        System.out.println("Ending Date: " + endingDate);
        System.out.println("Responsible: " + responsible);

        // Update task with new data
        Label taskLabel = (Label) currentEditTaskBox.getChildren().get(0);
        taskLabel.setText(taskName);
        // Update other fields of the task as needed

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

    private void addTaskToFirstColumn() {
        addTask(tasksVBox1);
    }

    private void addTask(VBox tasksVBox) {
        VBox taskBox = new VBox();
        taskBox.setSpacing(5);
        taskBox.getStyleClass().add("task-box");

        taskBox.setOnMouseClicked(this::handleTaskClick);

        Label taskLabel = new Label("Task");
        taskLabel.getStyleClass().add("task-label");

        FlowPane tagsPane = new FlowPane();
        tagsPane.getStyleClass().add("tags-pane");

        taskBox.getChildren().addAll(taskLabel, tagsPane);
        tasksVBox.getChildren().add(taskBox);
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
        editTaskNameField.clear();
        editTaskDescriptionArea.clear();
        editBeginningDatePicker.setValue(null);
        editEndingDatePicker.setValue(null);
        editResponsibleComboBox.setValue(null);
    }
}
