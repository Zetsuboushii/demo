module com.example.demo {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.demo to javafx.fxml;
    exports com.example.demo;

    opens Aufgabe_02_TiefesGUI to javafx.fxml;
    exports Aufgabe_02_TiefesGUI;
}