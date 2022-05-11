package org.bozdgn.service;

import org.bozdgn.model.Food;
import org.bozdgn.model.Meal;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MealService {
    public static List<Meal> listMeals(Connection conn) {


        try(PreparedStatement st = conn.prepareStatement(
                "SELECT mid, date, repast FROM meal")) {

            ResultSet rs = st.executeQuery();

            List<Meal> result = new ArrayList<>();
            while(rs.next()) {
                result.add(new Meal(
                        rs.getInt(1),
                        rs.getDate(2).toLocalDate(),
                        rs.getString(3)));
            }

            return result;
        } catch(SQLException e) {
            e.printStackTrace();
        }

        return null;  // TODO(bora): Handle errors properly so we can guarantee non-null return.
    }

    /** Returns -1 if entry not found. */
    public static int getMealID(
            Connection conn,
            LocalDate date,
            String repast
    ) {


        try(PreparedStatement st = conn.prepareStatement(
                "SELECT mid FROM meal WHERE date=? AND repast=?")) {

            st.setDate(1, Date.valueOf(date));
            st.setString(2, repast);

            ResultSet rs = st.executeQuery();

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

    public static void addMeal(
            Connection conn,
            LocalDate date,
            String repast
    ) {


        try(PreparedStatement st = conn.prepareStatement(
                "INSERT INTO meal (date, repast) VALUES (?,?)")) {

            st.setDate(1, Date.valueOf(date));
            st.setString(2, repast);

            st.executeUpdate();

        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public static void addMealFood(
            Connection conn,
            int mid,
            int fid
    ) {


        try(PreparedStatement st = conn.prepareStatement(
                "INSERT INTO has_food (mid, fid) VALUES (?,?)")) {

            st.setInt(1, mid);
            st.setInt(2, fid);

            st.executeUpdate();

        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<Food> getFoods(
            Connection conn,
            int mid
    ) {


        try(PreparedStatement st = conn.prepareStatement(
                "SELECT food.fid, food_name " +
                        "FROM meal JOIN has_food ON meal.fid=food.fid  WHERE mid=?")) {

            st.setInt(1, mid);

            ResultSet rs = st.executeQuery();

            List<Food> result = new ArrayList<>();
            while(rs.next()) {
                result.add(new Food(
                        rs.getInt(1),
                        rs.getString(2)));
            }

            return result;
        } catch(SQLException e) {
            e.printStackTrace();
        }

        return new ArrayList<>(0);
    }

    public static void removeMealByID(
            Connection conn,
            int mid
    ) {


        try(PreparedStatement st = conn.prepareStatement(
                "DELETE FROM meal WHERE mid=?")) {

            st.setInt(1, mid);
            st.executeUpdate();
        }
        catch(SQLException e) {
            e.printStackTrace();
        }
    }
}
