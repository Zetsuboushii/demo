module com.example.demo {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens com.example.demo to javafx.fxml;
    exports com.example.demo;

    opens Aufgabe_02_TiefesGUI to javafx.fxml;
    exports Aufgabe_02_TiefesGUI;

    opens Aufgabe_03_Audio to javafx.fxml;
    exports Aufgabe_03_Audio;
}