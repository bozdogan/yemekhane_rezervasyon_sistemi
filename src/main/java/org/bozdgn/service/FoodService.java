package org.bozdgn.service;

import org.bozdgn.model.Food;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FoodService {
    public static List<Food> listFoods(Connection conn) {

        try(PreparedStatement st = conn.prepareStatement(
                "SELECT fid, food_name FROM food")) {

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

        return null;  // TODO(bora): Handle errors properly so we can guarantee non-null return.
    }

    public static int getFoodID(Connection conn, String foodName) {

        try(PreparedStatement st = conn.prepareStatement(
                "SELECT fid FROM food WHERE food_name=?")) {

            st.setString(1, foodName);
            ResultSet rs = st.executeQuery();

            if(rs.next()) {
                return rs.getInt(1);
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public static void addFood(Connection conn, String foodName) {

        try(PreparedStatement st = conn.prepareStatement(
                "INSERT INTO food (food_name) VALUES (?)")) {

            st.setString(1, foodName);
            st.executeUpdate();

        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public static void removeFood(Connection conn, int fid) {

        try(PreparedStatement st = conn.prepareStatement(
                "DELETE FROM food WHERE fid=?")) {

            st.setInt(1, fid);
            st.executeUpdate();

        } catch(SQLException e) {
            e.printStackTrace();
        }
    }
}
