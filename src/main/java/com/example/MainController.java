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
    private void initialize() {
        // Initialize ComboBox items
        responsibleComboBox.setItems(FXCollections.observableArrayList("User 1", "User 2", "User 3"));
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
        showRightPanel("task");
    }

    @FXML
    private void handleAddTask1() {
        addTask(tasksVBox1);
    }

    @FXML
    private void handleAddTask2() {
        addTask(tasksVBox2);
    }

    @FXML
    private void handleAddTask3() {
        addTask(tasksVBox3);
    }

    @FXML
    private void handleCreateProject() {
        String projectName = projectNameField.getText();
        String projectDescription = projectDescriptionArea.getText();
        System.out.println("Project Created: " + projectName + " - " + projectDescription);
        // Add logic to create a new project
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

        // Add logic to create a new task
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
                break;
            case "task":
                taskCreateVBox.setVisible(true);
                taskCreateVBox.setManaged(true);
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
}
