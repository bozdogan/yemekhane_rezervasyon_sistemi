package org.bozdgn.service;

import java.sql.*;
import java.time.LocalDate;

public class StatisticsService {

    public static LocalDate getMostPurchasedDate(Database db) {
        Connection conn = db.connection;  // TODO(bora): Remove `Database` class.

        // TODO(bora): This should've been just a flag in the table.
        try(PreparedStatement st = conn.prepareStatement(
                "SELECT COUNT(*) as count, date FROM meal, has_meal " +
                    "WHERE meal.mid = has_meal.mid " +
                    "GROUP BY date " +
                    "ORDER BY count DESC")) {

            ResultSet rs = st.executeQuery();
            if(rs.next()) {
                return rs.getDate(2).toLocalDate();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String getMostServedFood(Database db) {
        Connection conn = db.connection;  // TODO(bora): Remove `Database` class.

        // TODO(bora): This should've been just a flag in the table.
        try(PreparedStatement st = conn.prepareStatement(
                "SELECT COUNT(*) as count, food_name FROM food, has_food "  +
                    "WHERE food.fid=has_food.fid GROUP BY food.fid ORDER BY count DESC")) {

            ResultSet rs = st.executeQuery();
            if(rs.next()) {
                return rs.getString(2);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
    public static int countPurchasedMeals(
            Database db,
            LocalDate startDate,
            LocalDate endDate
    ) {
        Connection conn = db.connection;  // TODO(bora): Remove `Database` class.

        // TODO(bora): This should've been just a flag in the table.
        try(PreparedStatement st = conn.prepareStatement(
                "SELECT COUNT(*) as count FROM has_meal " +
                    "WHERE date>=? AND date<=? GROUP BY pid")) {

            st.setDate(1, Date.valueOf(startDate));
            st.setDate(2, Date.valueOf(endDate));

            ResultSet rs = st.executeQuery();
            if(rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }
}
