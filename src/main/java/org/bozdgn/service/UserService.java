package org.bozdgn.service;

import org.bozdgn.model.User;

import java.sql.*;

public class UserService {
    /** Returns `null` if login fails. */
    public static User login(Connection conn, String pid, String password) {
        if(pid.equals("admin") && password.equals("admin")) {
            return new User("admin", "Admin", "Admin", true);
        }

        try(PreparedStatement st = conn.prepareStatement(
                "SELECT pid, firstname, lastname FROM users WHERE pid=? AND password=?")
        ) {
            st.setString(1, pid);
            st.setString(2, password);

            ResultSet rs = st.executeQuery();
            if(rs.next()) {
                return new User(
                    rs.getString(1),
                    rs.getString(2),
                    rs.getString(3),
                    false);
            }
        } catch(SQLException e) {
            System.out.println("SQL error while trying to login");
            e.printStackTrace();
        }

        // NOTE(bora): Authentication failed, return null
        return null;
    }
}
