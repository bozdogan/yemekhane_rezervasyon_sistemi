package org.bozdogan;

import javafx.scene.control.Alert;

public class AlertBox{
    public static void showWarning(String message){
        new Alert(Alert.AlertType.WARNING, message).show(); //@TODO implement a prettier alternative
    }
    public static void showWarningAndWait(String message){
        new Alert(Alert.AlertType.WARNING, message).showAndWait(); //@TODO implement a prettier alternative
    }
}
