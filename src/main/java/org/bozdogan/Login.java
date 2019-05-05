package org.bozdogan;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class Login{

    @FXML private TextField usernameTx;
    @FXML private PasswordField passwordTx;
    @FXML private Button loginBt;
    @FXML private Label statusLb;

    @FXML
    void login(ActionEvent event){
        try{
            // If login succeeds, one of the panels will replace current scene.
            Parent newRoot = null;
            String newTitle = null, login_name = null;

            if(usernameTx.getText().equals("admin") &&
                    passwordTx.getText().equals("admin")){ // check admin login

                login_name = "admin";
                // load admin panel
                newTitle = App.appName+" Admin Panel | User: "+login_name;
                newRoot = FXMLLoader.load(getClass().getResource("/adminpanel.fxml"));

            } else{ // check login

                // prepare statement
                App.database.prepare("SELECT * FROM users WHERE pid=? AND password=?");
                // fill the '?'s in statement
                HashMap<Integer, Object> data = new HashMap<>();
                data.put(1, usernameTx.getText());
                data.put(2, passwordTx.getText());

                // execute statement
                Map<String, Object> result = App.database.fetch(data);

                if(result.size()>0){
                    // login successful
                    login_name = result.get("firstname")+" "+result.get("lastname");

                    // load user panel
                    newTitle = App.appName+" Student Panel | User: "+login_name;
                    newRoot = FXMLLoader.load(getClass().getResource("/userpanel.fxml"));

                } else{
                    // login failed
                    AlertBox.showWarning("Incorrect username or password.");
                }
            }

            if(newRoot!=null){
                // store the username of the current user.
                App.currentUser = login_name;
                // change the title
                App.stage.setTitle(newTitle);
                // load the panel.
                App.stage.setScene(new Scene(newRoot));
            }

        } catch(SQLException|IOException e){ e.printStackTrace(); }
    }
}
