module com.example {
    requires transitive javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive java.sql;
    requires com.zaxxer.hikari;
    requires de.mkammerer.argon2;
    requires org.jfree.jfreechart;
    requires org.postgresql.jdbc;
    requires com.calendarfx.view;

    opens com.example to javafx.fxml;
    exports com.example;
    exports com.example.Entities;
}
