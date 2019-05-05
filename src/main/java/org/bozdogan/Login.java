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
            // prepare statement
            App.database.prepare("SELECT * FROM users WHERE pid=? AND password=?");
            // fill the '?'s in statement
            HashMap<Integer, Object> data = new HashMap<>();
            data.put(1, usernameTx.getText());
            data.put(2, passwordTx.getText());

            // execute statement
            Map<String, Object> result = App.database.fetch(data);

            // check login
            if(result.size()>0){
                // login successful
                String login_name = result.get("firstname")+" "+result.get("lastname");

                try{
                    Parent newRoot = null;
                    String newTitle;
                    // decide which scene to load based on the type of the user
                    if(/*@TODO admin? == */true){
                        newRoot = FXMLLoader.load(getClass().getResource("view/adminpanel.fxml"));
                        newTitle = App.appName+" Admin Panel | User: "+login_name;
                    } else{
                        newRoot = FXMLLoader.load(getClass().getResource("view/userpanel.fxml"));
                        newTitle = App.appName+" Student Panel | User: "+login_name;
                    }

                    if(newRoot!=null){
                        // store the username of the current user.
                        App.currentUser = login_name;
                        // change the title
                        App.stage.setTitle(newTitle);
                        // load the panel.
                        App.stage.setScene(new Scene(newRoot));
                    }
                } catch(IOException e){ e.printStackTrace(); }

            } else{
                AlertBox.showWarning("Incorrect username or password.");
            }

        } catch(SQLException e){ e.printStackTrace(); }
    }
}
