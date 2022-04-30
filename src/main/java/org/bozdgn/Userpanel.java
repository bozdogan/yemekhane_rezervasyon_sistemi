package org.bozdgn;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import javafx.scene.control.cell.PropertyValueFactory;
import org.bozdgn.model.Purchase;
import org.bozdgn.model.Reservation;

import java.net.URL;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDate;
import java.util.*;

public class Userpanel implements Initializable{

    @FXML private DatePicker reservationDate;

    @FXML private ToggleGroup repast;
    @FXML private RadioButton repastB;
    @FXML private RadioButton repastL;
    @FXML private RadioButton repastD;

    @FXML private ToggleGroup refectory;
    @FXML private RadioButton refectoryIe;
    @FXML private RadioButton refectoryYe;

    @FXML private Button applyBt;

    @FXML private TableView<Reservation> reservationTb;
    @FXML private TableColumn<Reservation, LocalDate> res_date;
    @FXML private TableColumn<Reservation, String> res_repast;
    @FXML private TableColumn<Reservation, String> res_refectory;

    @FXML private Button cancelSelectedBt;
    @FXML private Button purchaseBt;

    @FXML private TableView<Purchase> purchaseTb;
    @FXML private TableColumn<Purchase, LocalDate> purchase_date;
    @FXML private TableColumn<Purchase, String> purchase_repast;
    @FXML private TableColumn<Purchase, String> purchase_refectory;

    @FXML private Button changeRefectoryBt;

    @Override
    public void initialize(URL location, ResourceBundle resources){

        // INIT RESERVATIONS TABLEVIEW
        reservationTb.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        res_date.setCellValueFactory(new PropertyValueFactory<>("date"));
        res_repast.setCellValueFactory(new PropertyValueFactory<>("repast"));
        res_refectory.setCellValueFactory(new PropertyValueFactory<>("refectory"));
        updateReservationsTable();

        // INIT PURCHASE TABLEVIEW
        purchase_date.setCellValueFactory(new PropertyValueFactory<>("date"));
        purchase_repast.setCellValueFactory(new PropertyValueFactory<>("repast"));
        purchase_refectory.setCellValueFactory(new PropertyValueFactory<>("refectory"));
        updatePurchaseTable();



    }

    @FXML
    public void addReservation(){
        LocalDate resDate = reservationDate.getValue();

        String repastStr;
        RadioButton selectedRepast = (RadioButton) repast.getSelectedToggle();
        if(selectedRepast==repastB) repastStr="B";
        else if(selectedRepast==repastL) repastStr="L";
        else if(selectedRepast==repastD) repastStr = "D";
        else repastStr = null;

        String refectoryStr;
        RadioButton selectedRefectory = (RadioButton) refectory.getSelectedToggle();
        if(selectedRefectory==refectoryIe) refectoryStr="ieylul";
        else if(selectedRefectory==refectoryYe) refectoryStr="yemre";
        else refectoryStr = null;

        if(resDate==null || selectedRepast==null || selectedRefectory==null){
            AlertBox.showWarning("Please fill all the fields.");
            return;
        }

        try{
            // OBTAIN MEAL ID
            int meal_id;
            App.database.prepare("SELECT mid FROM meal WHERE date=? AND repast=?");
            Map<String, Object> result = App.database.fetch(new HashMap<Integer, Object>(){{
                 put(1, resDate);
                 put(2, repastStr);
            }});

            if(result.isEmpty()){ // if meal d.n.e, meal id cannot be obtained.
                AlertBox.showWarning("No meal available with specified date and repast.");
                return;
            }

            meal_id = (Integer) result.get("mid");

            // CHECK IF ALREADY RESERVED OR PURCHASED
            boolean already_on_the_list = false;
            App.database.prepare("SELECT COUNT(*) as count FROM reservations, meal " +
                    "WHERE reservations.mid=meal.mid " +
                    "    AND reservations.pid=? AND meal.date=? ");

            result = App.database.fetch(new HashMap<Integer, Object>(){{
                put(1, App.personId);
                put(2, resDate);
            }});

            if((Long) result.get("count")>0)
                already_on_the_list = true;

            App.database.prepare("SELECT COUNT(*) as count FROM has_meal, meal " +
                    "WHERE has_meal.mid=meal.mid " +
                    "    AND has_meal.pid=? AND meal.date=? ");

            result = App.database.fetch(new HashMap<Integer, Object>(){{
                put(1, App.personId);
                put(2, resDate);
            }});

            if((Long) result.get("count")>0)
                already_on_the_list = true;

            if(already_on_the_list){
                AlertBox.showWarning("Meal already reserved or purchased:\n" +
                        "    "+resDate+" "+repastStr);
                return;
            }

            // ADD RESERVATION ENTRY
            App.database.prepare("INSERT INTO reservations (pid, mid, refectory) " +
                    "VALUES (?,?,?)");
            int _affected = App.database.executeUpdate(new HashMap<Integer, Object>(){{
                put(1, App.personId);
                put(2, meal_id);
                put(3, refectoryStr);
            }});

            if(_affected>0)
                updateReservationsTable();

        } catch(SQLException e){ System.out.println("Bir şey oldu: "); e.printStackTrace(); }
    }

    @FXML
    public void cancelReservation(){
        ObservableList<Reservation> selectedRes = reservationTb.getSelectionModel().getSelectedItems();

        if(selectedRes.size()==0){
            AlertBox.showWarning("No reservations selected.");
            return;
        }

        String confirmationMessage = selectedRes.size()+" reservations selected. " +
                "These reservations will be cancelled.\n\nProceed?";
        if(AlertBox.showConfirmation("Confirmation", confirmationMessage)){
            try{
                // BUILD SQL QUERY
                StringBuilder query = new StringBuilder("DELETE FROM reservations WHERE " +
                        "pid="+App.personId+" AND (0");

                for(Reservation r: selectedRes){
                        // OBTAIN MEAL ID
                        int meal_id;
                        App.database.prepare("SELECT mid FROM meal WHERE date=? AND repast=?");
                        Map<String, Object> results = App.database.fetch(new HashMap<Integer, Object>(){{
                            put(1, r.getDate());
                            put(2, r.getRepast());
                        }});
                        meal_id = (Integer) results.get("mid");

                        // EXTEND QUERY
                        query.append(" OR mid=").append(meal_id);
                }

                query.append(")");

                // DELETE RESERVATION ENTRIES
                App.database.prepare(query.toString());
                int _affected = App.database.executeUpdate(null);

                if(_affected <= 0) {
                    // Probably an external interaction is made with the database
                    System.out.println("Rezervasyonu silemedik :/");
                }
                updateReservationsTable();

            } catch(SQLException e){ System.out.println("Bir şey oldu: "); e.printStackTrace(); }
        }
    }

    @FXML
    public void purchaseReservations(){
        try{
            // GET PRICE
            long resCount;
            App.database.prepare("SELECT COUNT(mid) as count FROM reservations WHERE pid=?");
            Map<String, Object> results = App.database.fetch(new HashMap<Integer, Object>(){{
                put(1, App.personId);
            }});

            resCount = (Long) results.get("count");

            String confirmationMessage = String.format("%d reservations has total price of\n"+
                    "    ₺%.2f\n\nProceed?", resCount, resCount*App.unitPrice);
            if(AlertBox.showConfirmation("Confirmation", confirmationMessage)){

                // GET RESERVATION INFO
                Map result = App.database.prepare("SELECT * FROM reservations WHERE pid=?").fetch(
                        new HashMap<Integer, Object>(){{put(1, App.personId);}}
                );

                int res_mid = (Integer) result.get("mid");
                String res_ref = (String) result.get("refectory");

                // DELETE RESERVATION ENTRY
                App.database.prepare("DELETE FROM reservations WHERE pid=?");
                int _affected_delete = App.database.executeUpdate(new HashMap<Integer, Object>(){{
                    put(1, App.personId);
                }});

                // ADD PURCHASE ENTRY
                App.database.prepare("INSERT INTO has_meal (pid, mid, refectory) " +
                        "VALUES (?,?,?)");
                int _affected_insert = App.database.executeUpdate(new HashMap<Integer, Object>(){{
                    put(1, App.personId);
                    put(2, res_mid);
                    put(3, res_ref);
                }});

                // :/
                if(_affected_delete!=_affected_insert)
                    System.out.println("Bir şey oldu...");

                // REFRESH
                if(_affected_insert>0){
                    updateReservationsTable();
                    updatePurchaseTable();
                }
            }

        } catch(SQLIntegrityConstraintViolationException e){ // Duplicate entry error
            updateReservationsTable();
            updatePurchaseTable();
        } catch(SQLException e){ System.out.println("Bir şey oldu: "); e.printStackTrace(); }
    }

    @FXML
    public void changeRefectory(){
        Purchase selected = purchaseTb.getSelectionModel().getSelectedItem();

        if(selected==null){
            AlertBox.showWarning("No purchases selected.");
            return;
        }

        try{
            String newRefectory = selected.getRefectory().equals("ieylul") ? "yemre" : "ieylul";

            String confirmationMessage = String.format("Current refectory is %s, you will change it with:\n"+
                    "    %s\n\nProceed?", selected.getRefectory(), newRefectory);
            if(AlertBox.showConfirmation("Confirmation", confirmationMessage)){

                // OBTAIN MEAL ID
                int meal_id;
                App.database.prepare("SELECT mid FROM meal WHERE date=? AND repast=?");
                Map<String, Object> results = App.database.fetch(new HashMap<Integer, Object>(){{
                    put(1, selected.getDate());
                    put(2, selected.getRepast());
                }});

                meal_id = (Integer) results.get("mid");

                // SET NEW REFECTORY
                App.database.prepare("UPDATE has_meal SET refectory=? WHERE pid=? AND mid=?");
                int _affected = App.database.executeUpdate(new HashMap<Integer, Object>(){{
                    put(1, newRefectory);
                    put(2, App.personId);
                    put(3, meal_id);
                }});

                if(_affected>0){
                    updatePurchaseTable();
                }
            }
        } catch(SQLException e){ e.printStackTrace(); }
    }

    private void updateReservationsTable(){
        try{
            // load the items
            ArrayList<Reservation> resList = new ArrayList<>();
            App.database.prepare("SELECT * FROM reservations, meal " +
                    "WHERE reservations.mid=meal.mid AND pid=?");
            List<Map<String, Object>> results = App.database.fetchAll(new HashMap<Integer, Object>(){{
                put(1, App.personId);
            }});

            for(Map<String, Object> result: results){
                resList.add(new Reservation(
                        ((java.sql.Date) result.get("date")).toLocalDate(),
                        (String) result.get("repast"),
                        (String) result.get("refectory")
                ));
            }

            reservationTb.getItems().setAll(resList);
        } catch(SQLException e){ e.printStackTrace(); }
    }

    private void updatePurchaseTable(){
        try{
            // load the items
            ArrayList<Purchase> purchaseList = new ArrayList<>();
            App.database.prepare("SELECT * FROM has_meal, meal " +
                    "WHERE has_meal.mid=meal.mid AND pid=?");
            List<Map<String, Object>> results = App.database.fetchAll(new HashMap<Integer, Object>(){{
                put(1, App.personId);
            }});

            for(Map<String, Object> result: results){
                purchaseList.add(new Purchase(
                        ((java.sql.Date) result.get("date")).toLocalDate(),
                        (String) result.get("repast"),
                        (String) result.get("refectory")
                ));
            }

            purchaseTb.getItems().setAll(purchaseList);
        } catch(SQLException e){ e.printStackTrace(); }
    }
}
