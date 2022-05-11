package org.bozdgn.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import org.bozdgn.model.User;
import org.bozdgn.service.UserService;

import java.io.IOException;

public class Login{

    @FXML private TextField usernameTx;
    @FXML private PasswordField passwordTx;
    @FXML private Button loginBt;
    @FXML private Label statusLb;

    @FXML
    void login(ActionEvent event) {
        try {
            User user = UserService.login(App.dbconn, usernameTx.getText(), passwordTx.getText());
            if(user != null) {
                if(user.isAdmin()) {
                    App.currentUser = "amdin";
                    App.stage.setTitle(App.appName+" Admin Panel | User: "+App.currentUser);
                    App.stage.setScene(new Scene(
                            FXMLLoader.load(getClass().getResource("/adminpanel.fxml"))
                    ));
                } else {
                    App.personId = usernameTx.getText();
                    App.currentUser = user.getFirstname() + " " + user.getLastname();
                    App.stage.setTitle(App.appName+" Student Panel | User: "+App.currentUser);
                    App.stage.setScene(new Scene(
                            FXMLLoader.load(getClass().getResource("/userpanel.fxml"))
                    ));
                }
            } else {
                AlertBox.showWarning("Incorrect username or password.");
            }
        } catch(IOException e) {
            System.out.println("Cannot load UI files");
            e.printStackTrace();
        }
    }
}
