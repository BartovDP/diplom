module com.example {
    requires transitive javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive java.sql;
    requires com.zaxxer.hikari;
    requires de.mkammerer.argon2;

    opens com.example to javafx.fxml;
    exports com.example;
}
