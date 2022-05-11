package org.bozdgn.service;

import java.sql.*;
import java.util.*;

/** Best API in the world! */
public class DatabaseService {
    public static Connection connect(
            String URL,
            String username,
            String password,
            String databaseName
    ) throws SQLException {

        Properties connectionInfo = new Properties();
        connectionInfo.setProperty("user", username);
        connectionInfo.setProperty("password", password);
        connectionInfo.setProperty("serverTimezone", "Europe/Istanbul"); //@ Timezone issue

        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // driver check

            return DriverManager.getConnection(
                    "jdbc:mysql://" + URL + "/" + databaseName +
                            "?useUnicode=true&characterEncoding=utf-8", connectionInfo);

        } catch(ClassNotFoundException e) {
            System.out.println("Mysql driver is not present.");
            System.err.println("Error: "+e.getMessage());
        }

        return null;
    }
}
