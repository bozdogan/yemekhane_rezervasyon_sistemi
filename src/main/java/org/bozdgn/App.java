package org.bozdgn;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application{
    static String appName = "EsYemek Otomasyonu";
    static double unitPrice = 1.25;

    static Stage stage;
    static Database database;
    static String currentUser;
    static String personId; // for database relations

    @Override
    public void start(Stage primaryStage) throws Exception{
        stage = primaryStage;

        database = new Database("localhost",
                "root",
                "",
                "refectory_database");

        if(!database.testConnection()){
            AlertBox.showWarningAndWait("Database connection failure!");
            Platform.exit();
            return;
        }


        Parent root = FXMLLoader.load(getClass().getResource("/login.fxml"));
        stage.setTitle(appName+" Login");
        stage.setScene(new Scene(root));
        stage.show();
    }

    @Override
    public void stop() throws Exception{
        super.stop();
        stage = null;
    }

    public static void main(String[] args){ launch(args); }
}
