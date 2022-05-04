package org.bozdgn.service;

import org.bozdgn.client.App;
import org.bozdgn.model.Reservation;

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

            st.executeUpdate();
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public static void cancelReservations(
            Database db,
            String pid,
            List<Reservation> meals
    ) {

        Connection conn = db.connection;  // TODO(bora): Remove `Database` class.

        try(PreparedStatement st = conn.prepareStatement(
                "DELETE FROM reservations WHERE pid=? AND ("
                + String.join(" OR ", Collections.nCopies(meals.size(), "mid=?"))
                + ")")) {

            st.setString(1, pid);

            for(int i = 0; i < meals.size(); ++i) {
                Reservation it = meals.get(i);
                int mid = MealService.getMealID(db, it.getDate(), it.getRepast());
                st.setInt(i + 2, mid);
            }

            st.executeUpdate();
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public static void makeSinglePurchase(
            Database db,
            String pid,
            int mid
    ) {
        Connection conn = db.connection;  // TODO(bora): Remove `Database` class.

        // TODO(bora): This should've been just a flag in the table. Not an entire table
        // with the same fields. A design oversight from young me..
        try(PreparedStatement stSel = conn.prepareStatement(
                "SELECT refectory FROM reservations WHERE pid=? AND mid=?")) {
            stSel.setString(1, pid);
            stSel.setInt(2, mid);

            ResultSet rs = stSel.executeQuery();
            if(!rs.next()) {
                // NOTE(bora): Reservation not found. Skip the rest.
                return;
            }
            String refectory = rs.getString(1);

            try(PreparedStatement stIns = conn.prepareStatement(
                        "INSERT INTO has_meal (pid, mid, refectory) " +
                                "VALUES (?,?,?)")) {

                stIns.setString(1, pid);
                stIns.setInt(2, mid);
                stIns.setString(3, refectory);

                if(stIns.executeUpdate() > 0) {
                    try(PreparedStatement stDel = conn.prepareStatement(
                            "DELETE FROM reservations WHERE pid=?")) {

                        stDel.setString(1, pid);
                        stDel.executeUpdate();
                    }
                }
            }
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
    public static List<Reservation> listUnpaid(
            Database db,
            String pid
    ) {
        Connection conn = db.connection;  // TODO(bora): Remove `Database` class.

        try(PreparedStatement st = conn.prepareStatement(
                "SELECT reservations.mid, date, repast, refectory "
                    + "FROM reservations JOIN meal ON reservations.mid = meal.mid "
                    + "WHERE pid=?")) {

            st.setString(1, pid);
            ResultSet rs = st.executeQuery();

            List<Reservation> result = new ArrayList<>();
            while(rs.next()) {
                result.add(new Reservation(
                        rs.getInt(1),
                        rs.getDate(2).toLocalDate(),
                        rs.getString(3),
                        rs.getString(4)));
            }

            return result;

        } catch(SQLException e) {
            e.printStackTrace();
        }

        return null;  // TODO(bora): Handle errors properly so we can guarantee non-null return.
    }

    /** Returns a list of reservations bought by given person. */
    public static List<Reservation> listPaid(
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

            List<Reservation> result = new ArrayList<>();
            while(rs.next()) {
                result.add(new Reservation(
                        rs.getDate(1).toLocalDate(),
                        rs.getString(2),
                        rs.getString(3)));
            }

            return result;

        } catch(SQLException e) {
            e.printStackTrace();
        }

        return null;  // TODO(bora): Handle errors properly so we can guarantee non-null return.
    }

    public static void changeReservationRefectory(
            Database db,
            String pid,
            int mid,
            String newRefectory
    ) {
        Connection conn = db.connection;  // TODO(bora): Remove `Database` class.

        try(PreparedStatement st = conn.prepareStatement(
                "UPDATE has_meal SET refectory=? WHERE pid=? AND mid=?")) {

            st.setString(1, newRefectory);
            st.setString(2, pid);
            st.setInt(3, mid);

        } catch(SQLException e) {
            e.printStackTrace();
        }
    }
}
