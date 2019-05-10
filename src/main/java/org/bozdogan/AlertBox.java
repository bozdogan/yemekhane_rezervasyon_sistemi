package org.bozdogan;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

import java.util.Optional;

public class AlertBox{
    public static void showWarning(String message){
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.show();
    }

    public static void showWarningAndWait(String message){
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }

    public static boolean showConfirmation(String title, String message){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

//        alert.getButtonTypes().setAll(
//                new ButtonType("Yes", ButtonBar.ButtonData.OK_DONE),
//                new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE)
//        );

        Optional<ButtonType> result = alert.showAndWait();
        return result.get()==ButtonType.OK;
    }
}
