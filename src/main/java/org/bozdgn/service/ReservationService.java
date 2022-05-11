package org.bozdgn.service;

import org.bozdgn.model.Reservation;
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

            st.executeUpdate();  // TODO(bora): Should I log if nothing is deleted?
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public static void makePurchase(
            Database db,
            String pid,
            int mid
    ) {
        Connection conn = db.connection;  // TODO(bora): Remove `Database` class.

        // TODO(bora): This should've been just a flag in the table.
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
                            "DELETE FROM reservations WHERE pid=? AND mid=?")) {

                        stDel.setString(1, pid);
                        stDel.setInt(2, mid);
                        stDel.executeUpdate();
                    }
                }
            }
        } catch(SQLIntegrityConstraintViolationException e) {
            // NOTE(bora): It's already inserted. Do nothing.
        } catch (SQLException e) {
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
                "SELECT reservations.mid, date, repast, refectory "
                    + "FROM reservations JOIN meal ON reservations.mid = meal.mid "
                    + "WHERE pid=?")) {

            st.setString(1, pid);
            ResultSet rs = st.executeQuery();

            List<ReservedMeal> result = new ArrayList<>();
            while(rs.next()) {
                result.add(new ReservedMeal(
                        pid,
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

    /** Returns a list of reservations made that are not purchased yet. */
    public static List<ReservedMeal> listUnpaidAll(
            Database db
    ) {
        Connection conn = db.connection;  // TODO(bora): Remove `Database` class.

        try(PreparedStatement st = conn.prepareStatement(
                "SELECT pid, reservations.mid, date, repast, refectory "
                    + "FROM reservations JOIN meal ON reservations.mid = meal.mid")) {

            ResultSet rs = st.executeQuery();

            List<ReservedMeal> result = new ArrayList<>();
            while(rs.next()) {
                result.add(new ReservedMeal(
                        rs.getString(1),
                        rs.getInt(2),
                        rs.getDate(3).toLocalDate(),
                        rs.getString(4),
                        rs.getString(5)));
            }

            return result;

        } catch(SQLException e) {
            e.printStackTrace();
        }

        return null;  // TODO(bora): Handle errors properly so we can guarantee non-null return.
    }

    /** Returns a list of reservations purchased by given person. */
    public static List<ReservedMeal> listPaid(
            Database db,
            String pid
    ) {
        Connection conn = db.connection;  // TODO(bora): Remove `Database` class.

        try(PreparedStatement st = conn.prepareStatement(
                "SELECT has_meal.mid, date, repast, refectory "
                    + "FROM has_meal JOIN meal ON has_meal.mid = meal.mid "
                    + "WHERE pid=?")) {

            st.setString(1, pid);
            ResultSet rs = st.executeQuery();

            List<ReservedMeal> result = new ArrayList<>();
            while(rs.next()) {
                result.add(new ReservedMeal(
                        pid,
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

    /** Returns a list of reservations purchased. */
    public static List<ReservedMeal> listPaidAll(
            Database db
    ) {
        Connection conn = db.connection;  // TODO(bora): Remove `Database` class.

        try(PreparedStatement st = conn.prepareStatement(
                "SELECT pid, has_meal.mid, date, repast, refectory "
                    + "FROM has_meal JOIN meal ON has_meal.mid = meal.mid")) {

            ResultSet rs = st.executeQuery();

            List<ReservedMeal> result = new ArrayList<>();
            while(rs.next()) {
                result.add(new ReservedMeal(
                        rs.getString(1),
                        rs.getInt(2),
                        rs.getDate(3).toLocalDate(),
                        rs.getString(4),
                        rs.getString(5)));
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

            st.executeUpdate();

        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public static void batchCancelReservationsByIDs(
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

    public static void batchCancelReservationsByDate(
            Database db,
            List<ReservedMeal> meals
    ) {
        List<Reservation> reservations = new ArrayList<>();
        for(ReservedMeal it: meals) {
            assert it.getDate() != null;
            assert it.getRepast() != null;

            int mid = MealService.getMealID(db, it.getDate(), it.getRepast());
            reservations.add(new Reservation(it.getPid(), mid, null));
        }
        batchCancelReservationsByIDs(db, reservations);
    }

    public static void batchCancelReservationsByIDs(
            Database db,
            List<Reservation> meals
    ) {

        Connection conn = db.connection;  // TODO(bora): Remove `Database` class.

        try(PreparedStatement st = conn.prepareStatement(
                "DELETE FROM reservations WHERE "
                + String.join(" OR ",
                        Collections.nCopies(meals.size(), "(pid=? AND mid=?)"))
        )) {

            for(int i = 0; i < meals.size(); ++i) {
                Reservation it = meals.get(i);
                st.setString(i*2, it.getPid());
                st.setInt(i*2 + 1, it.getMid());
            }

            st.executeUpdate();
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    /** This removes PAID reservations */
    public static void batchRemovePurchaseByIDs (
            Database db,
            List<Reservation> meals
    ) {

        Connection conn = db.connection;  // TODO(bora): Remove `Database` class.

        try(PreparedStatement st = conn.prepareStatement(
                "DELETE FROM has_meal WHERE "
                + String.join(" OR ",
                        Collections.nCopies(meals.size(), "(pid=? AND mid=?)"))
        )) {

            for(int i = 0; i < meals.size(); ++i) {
                Reservation it = meals.get(i);
                st.setString(i*2, it.getPid());
                st.setInt(i*2 + 1, it.getMid());
            }

            st.executeUpdate();
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }


    public static void batchChangeReservationRefectory(
            Database db,
            List<Reservation> meals,
            String newRefectory
    ) {
        Connection conn = db.connection;  // TODO(bora): Remove `Database` class.

        try(PreparedStatement st = conn.prepareStatement(
                "UPDATE has_meal SET refectory=? WHERE" + String.join(" OR ",
                        Collections.nCopies(meals.size(), "(pid=? AND mid=?)"))
        )) {

            st.setString(1, newRefectory);

            for(int i = 0; i < meals.size(); ++i) {
                Reservation it = meals.get(i);
                st.setString(i*2 + 1, it.getPid());
                st.setInt(i*2 + 2, it.getMid());
            }

            st.executeUpdate();

        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

}

