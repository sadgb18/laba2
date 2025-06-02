module com.example.metro {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.postgresql.jdbc;


    opens com.example.metro to javafx.fxml;
    exports com.example.metro;
}