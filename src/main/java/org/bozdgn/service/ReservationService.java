package org.bozdgn.service;

import org.bozdgn.model.ReservedMeal;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReservationService {
    /** Returns `true` if there is an entry in "meals" table with provided info. */
    public static boolean isReserved(
            Database db,
            String pid,
            LocalDate date,
            String repast
    ) {
        Connection conn = db.connection;  // TODO(bora): Remove `Database` class.

        try(PreparedStatement st = conn.prepareStatement(
                "SELECT COUNT(*) as count FROM reservations, meal " +
                    "WHERE reservations.mid=meal.mid " +
                    "AND reservations.pid=? AND meal.date=? AND meal.repast=?")) {

            st.setString(1, pid);
            st.setDate(2, Date.valueOf(date));
            st.setString(3, repast);

            ResultSet rs = st.executeQuery();
            
            if(rs.next()) {
                int count = rs.getInt(1);
                return count > 0;
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /** Returns number of rows affected by operation. Should be either 0 or 1. */
    public static int makeReservation(
            Database db,
            String pid,
            int mid,
            String refectory
    ) {
        Connection conn = db.connection;  // TODO(bora): Remove `Database` class.

        try(PreparedStatement st = conn.prepareStatement(
                "INSERT INTO reservations (pid, mid, refectory) " +
                    "VALUES (?,?,?)")) {

            st.setString(1, pid);
            st.setInt(2, mid);
            st.setString(3, refectory);

            return st.executeUpdate();
        } catch(SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public static void cancelReservation(
            Database db,
            String pid,
            LocalDate date,
            String repast
    ) {
        Connection conn = db.connection;  // TODO(bora): Remove `Database` class.

        try(PreparedStatement st = conn.prepareStatement(
                "DELETE FROM reservations WHERE pid=? AND mid=?")) {
            int mid = MealService.getMealID(db, date, repast);
            st.setString(1, pid);
            st.setInt(2, mid);

            int affected = st.executeUpdate();
            if(affected <= 0) {
                // Probably an external interaction is made with the database
            }

        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public static void cancelReservations(
            Database db,
            String pid,
            List<ReservedMeal> meals
    ) {

        Connection conn = db.connection;  // TODO(bora): Remove `Database` class.

        try(PreparedStatement st = conn.prepareStatement(
                "DELETE FROM reservations WHERE pid=? AND ("
                + String.join(" OR ", Collections.nCopies(meals.size(), "mid=?"))
                + ")")) {

            st.setString(1, pid);

            for(int i = 0; i < meals.size(); ++i) {
                ReservedMeal it = meals.get(i);
                int mid = MealService.getMealID(db, it.getDate(), it.getRepast());
                st.setInt(i + 2, mid);
            }

            st.executeUpdate();
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public static void purchaseReservation(
            Database db,
            String pid,
            int mid
    ) {
        Connection conn = db.connection;  // TODO(bora): Remove `Database` class.

        try(PreparedStatement st = conn.prepareStatement(
                "")) {
            
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    /** Returns number of reservations made by given person that are not purchased yet. */
    public static int countUnpaid(
            Database db,
            String pid
    ) {
        Connection conn = db.connection;  // TODO(bora): Remove `Database` class.

        try(PreparedStatement st = conn.prepareStatement(
                "SELECT COUNT(mid) as count FROM reservations WHERE pid=?")) {

            st.setString(1, pid);
            ResultSet rs = st.executeQuery();
            if(rs.next()) {
                return rs.getInt(1);
            }
            
        } catch(SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    /** Returns a list of reservations made by given person that are not purchased yet. */
    public static List<ReservedMeal> listUnpaid(
            Database db,
            String pid
    ) {
        Connection conn = db.connection;  // TODO(bora): Remove `Database` class.

        try(PreparedStatement st = conn.prepareStatement(
                "SELECT date, repast, refectory "
                    + "FROM reservations JOIN meal ON reservations.mid = meal.mid "
                    + "WHERE pid=?")) {

            st.setString(1, pid);
            ResultSet rs = st.executeQuery();

            List<ReservedMeal> result = new ArrayList<>();
            while(rs.next()) {
                result.add(new ReservedMeal(
                        rs.getDate(1).toLocalDate(),
                        rs.getString(2),
                        rs.getString(3)));
            }

            return result;

        } catch(SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /** Returns a list of reservations bought by given person. */
    public static List<ReservedMeal> listPaid(
            Database db,
            String pid
    ) {
        Connection conn = db.connection;  // TODO(bora): Remove `Database` class.

        try(PreparedStatement st = conn.prepareStatement(
                "SELECT date, repast, refectory "
                    + "FROM has_meal JOIN meal ON has_meal.mid = meal.mid "
                    + "WHERE pid=?")) {

            st.setString(1, pid);
            ResultSet rs = st.executeQuery();

            List<ReservedMeal> result = new ArrayList<>();
            while(rs.next()) {
                result.add(new ReservedMeal(
                        rs.getDate(1).toLocalDate(),
                        rs.getString(2),
                        rs.getString(3)));
            }

            return result;

        } catch(SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void changeReservationRefectory(
            Database db,
            String pid,
            int mid,
            String newRefectory
    ) {
        Connection conn = db.connection;  // TODO(bora): Remove `Database` class.

        try(PreparedStatement st = conn.prepareStatement(
                "")) {
            
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

}
