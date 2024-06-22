package com.example;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    private static Stage primaryStage;
    //private static MainController CurrrentController;


    @Override
    public void start(Stage primaryStage) throws IOException {
        App.primaryStage = primaryStage;
        DatabaseManager.switchDatabase(true);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/login.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root, 1280, 720);
        //scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setTitle("Project Management App");
        primaryStage.show();
    }

    @Override
    public void stop() {
       //DatabaseManager.close();
        MainController.stopListening();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static void setRoot(String fxml) throws IOException {
        FXMLLoader loader = new FXMLLoader(App.class.getResource(fxml));
        Parent root = loader.load();
        //CurrrentController = loader.getController();
        primaryStage.getScene().setRoot(root);
    }
}
