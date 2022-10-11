module com.example.fxclient {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.fxclient to javafx.fxml;
    exports com.example.fxclient;
}