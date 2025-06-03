module com.quickcalc {
    requires javafx.controls;
    requires javafx.fxml;
    // Temporarily commented out for Stage 2 development
    // requires com.fasterxml.jackson.databind;
    
    opens com.quickcalc to javafx.fxml;
    opens com.quickcalc.controllers to javafx.fxml;
    
    exports com.quickcalc;
    exports com.quickcalc.controllers;
    exports com.quickcalc.models;
    exports com.quickcalc.utils;
    exports com.quickcalc.constants;
    exports com.quickcalc.views.components;
}
