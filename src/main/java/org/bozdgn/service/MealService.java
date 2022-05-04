package org.bozdgn.service;

import java.sql.*;
import java.time.LocalDate;

public class MealService {
    /** Returns -1 if entry not found. */
    public static int getMealID(
            Database db,
            LocalDate date,
            String repast
    ) {
        Connection conn = db.connection;  // TODO(bora): Remove `Database` class.

        try(PreparedStatement st = conn.prepareStatement(
                "SELECT mid FROM meal WHERE date=? AND repast=?")) {

            st.setDate(1, Date.valueOf(date));
            st.setString(2, repast);

            ResultSet rs = st.executeQuery();
            ResultSetMetaData metadata = rs.getMetaData();

            if(rs.next()) {
                int mealID = rs.getInt(1);
                return mealID;
            }
        }
        catch(SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }
}
