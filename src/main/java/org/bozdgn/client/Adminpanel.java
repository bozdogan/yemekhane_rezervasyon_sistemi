package org.bozdgn.client;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDate;
import java.util.*;

import javafx.scene.control.cell.PropertyValueFactory;
import org.bozdgn.client.data.*;

public class Adminpanel implements Initializable{

    @FXML private TableView<AdminReservation> reservationTb;
    @FXML private TableColumn<AdminReservation, String> reservation_pid;
    @FXML private TableColumn<AdminReservation, Integer> reservation_mid;
    @FXML private TableColumn<AdminReservation, LocalDate> reservation_date;
    @FXML private TableColumn<AdminReservation, String> reservation_repast;
    @FXML private TableColumn<AdminReservation, String> reservation_refectory;

    @FXML private Button man_cancelResBt;
    @FXML private Button man_purchaseBt;
    @FXML private Button man_removeResBt;

    @FXML private TableView<AdminPurchase> purchaseTb;
    @FXML private TableColumn<AdminPurchase, String> purchase_pid;
    @FXML private TableColumn<AdminPurchase, Integer> purchase_mid;
    @FXML private TableColumn<AdminPurchase, LocalDate> purchase_date;
    @FXML private TableColumn<AdminPurchase, String> purchase_repast;
    @FXML private TableColumn<AdminPurchase, String> purchase_refectory;

    @FXML private ToggleGroup refectory;
    @FXML private RadioButton refectoryIe;
    @FXML private RadioButton refectoryYe;

    @FXML private Button man_changeRefBt;
    @FXML private Button man_removePurchaseBt;

    // ---

    @FXML private TableView<Meal> mealsTb;
    @FXML private TableColumn<Meal, Integer> meals_mid;
    @FXML private TableColumn<Meal, LocalDate> meals_date;
    @FXML private TableColumn<Meal, String> meals_repast;

    @FXML private Button removeMealBt;

    @FXML private ComboBox<Food> meal_foodCb1;
    @FXML private ComboBox<Food> meal_foodCb2;
    @FXML private ComboBox<Food> meal_foodCb3;
    @FXML private ComboBox<Food> meal_foodCb4;

    @FXML private Button meal_clearSelBt;
    @FXML private DatePicker addMealDate;

    @FXML private ToggleGroup repast;
    @FXML private RadioButton repastB;
    @FXML private RadioButton repastL;
    @FXML private RadioButton repastD;

    @FXML private ComboBox<Food> add_foodCb1;
    @FXML private ComboBox<Food> add_foodCb2;
    @FXML private ComboBox<Food> add_foodCb3;
    @FXML private ComboBox<Food> add_foodCb4;
    @FXML private Button addMealBt;

    @FXML private TableView<Food> foodsTb;
    @FXML private TableColumn<Food, Integer> food_fid;
    @FXML private TableColumn<Food, String> food_foodName;

    @FXML private Button removeFoodBt;
    @FXML private Button food_ReloadBt;

    // ---

    @FXML private TextField foodNameTx;
    @FXML private Button addFoodBt;

    @FXML private Label mostPurchasedDateLb;
    @FXML private Label mostPurchasedDayLb;
    @FXML private Label mostServedMealLb;

    @FXML private DatePicker stat_startDt;
    @FXML private DatePicker stat_endDt;

    @FXML private Label countLb1;
    @FXML private Label countLb2;
    @FXML private Label countLb3;

    @Override
    public void initialize(URL location, ResourceBundle resources){

        // INIT RESERVATIONS TABLEVIEW
        reservationTb.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        reservation_pid.setCellValueFactory(new PropertyValueFactory<>("pid"));
        reservation_mid.setCellValueFactory(new PropertyValueFactory<>("mid"));
        reservation_date.setCellValueFactory(new PropertyValueFactory<>("date"));
        reservation_repast.setCellValueFactory(new PropertyValueFactory<>("repast"));
        reservation_refectory.setCellValueFactory(new PropertyValueFactory<>("refectory"));
        updateReservationsTable();

        // INIT PURCHASE TABLEVIEW
        purchaseTb.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        purchase_pid.setCellValueFactory(new PropertyValueFactory<>("pid"));
        purchase_mid.setCellValueFactory(new PropertyValueFactory<>("mid"));
        purchase_date.setCellValueFactory(new PropertyValueFactory<>("date"));
        purchase_repast.setCellValueFactory(new PropertyValueFactory<>("repast"));
        purchase_refectory.setCellValueFactory(new PropertyValueFactory<>("refectory"));
        updatePurchaseTable();

        // INIT MEALS TABLEVIEW
        meals_mid.setCellValueFactory(new PropertyValueFactory<>("mid"));
        meals_date.setCellValueFactory(new PropertyValueFactory<>("date"));
        meals_repast.setCellValueFactory(new PropertyValueFactory<>("repast"));
        updateMealsTable();

        // INIT FOODS TABLEVIEW
        foodsTb.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        food_fid.setCellValueFactory(new PropertyValueFactory<>("fid"));
        food_foodName.setCellValueFactory(new PropertyValueFactory<>("foodName"));
        updateFoodsTable();

        // INIT FOOD SELECTIONS
        mealsTb.getSelectionModel().getSelectedCells()
                .addListener((ListChangeListener<? super TablePosition>) e -> fetchFoodsOfMeal());

        // INIT STATISTICS DATE PICKERS
        stat_startDt.valueProperty().addListener(
                (observable, oldValue, newValue) -> loadDateIntervalStatistics());
        stat_endDt.valueProperty().addListener(
                (observable, oldValue, newValue) -> loadDateIntervalStatistics());
    }

    // ---
    // MANAGEMENT TAB
    // --------------

    @FXML
    public void cancelReservation(){
        ObservableList<AdminReservation> selectedRes = reservationTb.getSelectionModel().getSelectedItems();

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
                        "(0");

                for(AdminReservation r: selectedRes){
                    // OBTAIN MEAL ID
                    int meal_id;
                    App.database.prepare("SELECT mid FROM meal WHERE date=? AND repast=?");
                    Map<String, Object> results = App.database.fetch(new HashMap<Integer, Object>(){{
                        put(1, r.getDate());
                        put(2, r.getRepast());
                    }});
                    meal_id = (Integer) results.get("mid");

                    // EXTEND QUERY
                    query.append(" OR (pid='").append(r.getPid()).append("' AND mid=").append(meal_id).append(")");
                }

                query.append(")");

                // DELETE RESERVATION ENTRIES
                App.database.prepare(query.toString());
                int _affected = App.database.executeUpdate(null);

                if(_affected<1){
                    // Probably an external interaction is made with the database
                    System.out.println("Rezervasyonu silemedik :/");
                }

                updateReservationsTable();
            } catch(SQLException e){ System.out.println("Bir şey oldu: "); e.printStackTrace(); }
        }
    }

    @FXML
    public void setReservationsPurchased(){
        ObservableList<AdminReservation> selectedRes = reservationTb.getSelectionModel().getSelectedItems();
        int numberOfSelectedItems = selectedRes.size();

        if(numberOfSelectedItems==0){
            AlertBox.showWarning("No reservations selected.");
            return;
        }

        String confirmationMessage = String.format("%d reservations will be marked as purchased." +
                "\n\nProceed?", numberOfSelectedItems);
        if(AlertBox.showConfirmation("Confirmation", confirmationMessage)){
            try{
                for(AdminReservation res: selectedRes){

                    // DELETE RESERVATION ENTRY
                    App.database.prepare("DELETE FROM reservations " +
                            "WHERE pid=? AND mid=? AND refectory=?");
                    int _affected_delete = App.database.executeUpdate(new HashMap<Integer, Object>(){{
                        put(1, res.getPid());
                        put(2, res.getMid());
                        put(3, res.getRefectory());
                    }});

                    // ADD PURCHASE ENTRY
                    App.database.prepare("INSERT INTO has_meal (pid, mid, refectory) "+
                            "VALUES (?,?,?)");
                    int _affected_insert = App.database.executeUpdate(new HashMap<Integer, Object>(){{
                        put(1, res.getPid());
                        put(2, res.getMid());
                        put(3, res.getRefectory());
                    }});

                    // :/
                    if(_affected_delete!=_affected_insert)
                        System.out.println("Bir şey oldu Amdin Bey..");

                    // REFRESH
                    if(_affected_insert>0){
                        System.out.println(numberOfSelectedItems+" reservations have marked as purchased.");
                        updateReservationsTable();
                        updatePurchaseTable();
                    }
                }
            } catch(SQLIntegrityConstraintViolationException e){ // Duplicate entry error
                updateReservationsTable();
                updatePurchaseTable();
            } catch(SQLException e){ System.out.println("Bir şey oldu: ");e.printStackTrace(); }
        }
    }

    @FXML
    public void removeReservations(){
        ObservableList<AdminPurchase> selectedRes = purchaseTb.getSelectionModel().getSelectedItems();
        int numberOfSelectedItems = selectedRes.size();

        if(numberOfSelectedItems==0){
            AlertBox.showWarning("No reservations selected.");
            return;
        }

        String confirmationMessage = String.format("%d reservations will be removed." +
                "\n\nProceed?", numberOfSelectedItems);
        if(AlertBox.showConfirmation("Confirmation", confirmationMessage)){
            try{
                int failCount = 0;

                App.database.prepare("DELETE FROM reservations WHERE pid=? AND mid=? AND refectory=?");
                for(AdminPurchase purchase: selectedRes){
                    int _affected = App.database.executeUpdate(new HashMap<Integer, Object>(){{
                        put(1, purchase.getPid());
                        put(2, purchase.getMid());
                        put(3, purchase.getRefectory());
                    }});

                    if(_affected<1){
                        failCount++;
                    }
                }

                if(failCount>0){
                    AlertBox.showWarning(failCount+" operations have failed.");
                }

                updateReservationsTable();
            } catch(SQLException e){ e.printStackTrace(); }
        }
    }

    @FXML
    public void changeRefectory(){
        ObservableList<AdminPurchase> selectedPurchases = purchaseTb.getSelectionModel().getSelectedItems();
        int numberOfSelectedItems = selectedPurchases.size();

        if(numberOfSelectedItems==0){
            AlertBox.showWarning("No purchases selected.");
            return;
        }

        String newRefectory;
        RadioButton selectedRefectory = (RadioButton) refectory.getSelectedToggle();
        if(selectedRefectory==refectoryIe) newRefectory = "ieylul";
        else if(selectedRefectory==refectoryYe) newRefectory = "yemre";
        else newRefectory = null;

        if(newRefectory==null){
            AlertBox.showWarning("No refectory specified.");
            return;
        }

        String confirmationMessage = String.format("Refectory of %d meals will be changed to:\n" +
                "    %s\n\nProceed?", numberOfSelectedItems, newRefectory);
        if(AlertBox.showConfirmation("Confirmation", confirmationMessage)){
            try{
                int failCount = 0;

                App.database.prepare("UPDATE has_meal SET refectory=? WHERE pid=? AND mid=?");
                for(AdminPurchase purchase: selectedPurchases){
                    int _affected = App.database.executeUpdate(new HashMap<Integer, Object>(){{
                        put(1, newRefectory);
                        put(2, purchase.getPid());
                        put(3, purchase.getMid());
                    }});

                    if(_affected<1){
                        failCount++;
                    }
                }

                if(failCount>0){
                    AlertBox.showWarning(failCount+" operations have failed.");
                }

                updatePurchaseTable();
            } catch(SQLException e){ e.printStackTrace(); }
        }
    }

    @FXML
    public void removePurchases(){
        ObservableList<AdminPurchase> selectedPurchases = purchaseTb.getSelectionModel().getSelectedItems();
        int numberOfSelectedItems = selectedPurchases.size();

        if(numberOfSelectedItems==0){
            AlertBox.showWarning("No purchases selected.");
            return;
        }

        String confirmationMessage = String.format("%d purchases will be removed." +
                "\n\nProceed?", numberOfSelectedItems);
        if(AlertBox.showConfirmation("Confirmation", confirmationMessage)){
            try{
                int failCount = 0;

                App.database.prepare("DELETE FROM has_meal WHERE pid=? AND mid=? AND refectory=?");
                for(AdminPurchase purchase: selectedPurchases){
                    int _affected = App.database.executeUpdate(new HashMap<Integer, Object>(){{
                        put(1, purchase.getPid());
                        put(2, purchase.getMid());
                        put(3, purchase.getRefectory());
                    }});

                    if(_affected<1){
                        failCount++;
                    }
                }

                if(failCount>0){
                    AlertBox.showWarning(failCount+" operations have failed.");
                }

                updatePurchaseTable();
            } catch(SQLException e){ e.printStackTrace(); }
        }
    }

    // ---
    // EDIT MEALS TAB
    // --------------

    @FXML
    public void removeSelectedMeal(){
        Meal selectedMeal = mealsTb.getSelectionModel().getSelectedItem();
        String confirmationMessage = String.format("Meal will be removed:\n" +
                "     %s %s\n\nProceed?", selectedMeal.getDate(), selectedMeal.getRepast());
        if(AlertBox.showConfirmation("Confirmation", confirmationMessage)){
            try{
                App.database.prepare("DELETE FROM meal WHERE mid=?");

                int _affected = App.database.executeUpdate(new HashMap<Integer, Object>(){{
                    put(1, selectedMeal.getMid());
                }});

                if(_affected<1){
                    AlertBox.showWarning("Operation has failed.");
                }

                updateMealsTable();

            } catch(SQLException e){ e.printStackTrace(); }
        }
    }

    @FXML
    public void clearMealSelection(){
        try{
            meal_foodCb1.getSelectionModel().clearSelection();
            meal_foodCb2.getSelectionModel().clearSelection();
            meal_foodCb3.getSelectionModel().clearSelection();
            meal_foodCb4.getSelectionModel().clearSelection();
            mealsTb.getSelectionModel().clearSelection();
        } catch(NullPointerException e){/* :) */}
    }

    @FXML
    public void addMeal(){
        LocalDate mealDate = addMealDate.getValue();

        String repastStr;
        RadioButton selectedRepast = (RadioButton) repast.getSelectedToggle();
        if(selectedRepast==repastB) repastStr="B";
        else if(selectedRepast==repastL) repastStr="L";
        else if(selectedRepast==repastD) repastStr = "D";
        else repastStr = null;

        int food1Str = add_foodCb1.getSelectionModel().getSelectedItem().getFid();
        int food2Str = add_foodCb2.getSelectionModel().getSelectedItem().getFid();
        int food3Str = add_foodCb3.getSelectionModel().getSelectedItem().getFid();
        int food4Str = add_foodCb4.getSelectionModel().getSelectedItem().getFid();

        boolean _success_meal, _success_has_food;

        try{
            // Insert meal entry
            App.database.prepare("INSERT INTO meal (date, repast) VALUES (?,?)");

            _success_meal = App.database.executeUpdate(new HashMap<Integer, Object>(){{
                put(1, mealDate);
                put(2, repastStr);
            }})>0;

            // OBTAIN MEAL ID
            int meal_id;
            App.database.prepare("SELECT mid FROM meal WHERE date=? AND repast=?");
            Map<String, Object> result = App.database.fetch(new HashMap<Integer, Object>(){{
                put(1, mealDate);
                put(2, repastStr);
            }});

            meal_id = (Integer) result.get("mid");

            // Insert food relations
            App.database.prepare("INSERT INTO has_food (mid, fid) VALUES (?,?)");

            _success_has_food = App.database.executeUpdate(new HashMap<Integer, Object>(){{
                put(1, meal_id);
                put(2, food1Str);
            }})>0;
            _success_has_food = App.database.executeUpdate(new HashMap<Integer, Object>(){{
                put(1, meal_id);
                put(2, food2Str);
            }})>0 && _success_has_food;
            _success_has_food = App.database.executeUpdate(new HashMap<Integer, Object>(){{
                put(1, meal_id);
                put(2, food3Str);
            }})>0 && _success_has_food;
            _success_has_food = App.database.executeUpdate(new HashMap<Integer, Object>(){{
                put(1, meal_id);
                put(2, food4Str);
            }})>0 && _success_has_food;

            if(!_success_has_food){
                App.database.prepare("DELETE ROM has_food WHERE mid="+meal_id).execute();
            }

            if(!(_success_meal && _success_has_food)){
                AlertBox.showWarning("Operation(s) failed.");
            }

            updateMealsTable();

        } catch(SQLException e){ e.printStackTrace(); }

    }

    @FXML
    public void removeSelectedFoods(){
        ObservableList<Food> selectedFoods = foodsTb.getSelectionModel().getSelectedItems();
        int numberOfSelectedItems = selectedFoods.size();

        if(numberOfSelectedItems==0){
            AlertBox.showWarning("No foods selected.");
            return;
        }

        String confirmationMessage = String.format("%d foods will be removed." +
                "\n\nProceed?", numberOfSelectedItems);
        if(AlertBox.showConfirmation("Confirmation", confirmationMessage)){
            try{
                int failCount = 0;

                App.database.prepare("DELETE FROM food WHERE fid=?");
                for(Food food: selectedFoods){
                    int _affected = App.database.executeUpdate(new HashMap<Integer, Object>(){{
                        put(1, food.getFid());
                    }});

                    if(_affected<1){
                        failCount++;
                    }
                }

                if(failCount>0){
                    AlertBox.showWarning(failCount+" operations have failed.");
                }

                updateFoodsTable();
            } catch(SQLException e){ e.printStackTrace(); }
        }}

    @FXML
    public void addFood(){
        String foodName = foodNameTx.getText();

        if(foodName.equals(""))
            return;

        try{
            // Insert meal entry
            App.database.prepare("INSERT INTO food (food_name) VALUES (?)");

            if(App.database.executeUpdate(new HashMap<Integer, Object>(){{
                put(1, foodName);
            }})==0){
                System.out.println("Olmadi..");
            }

            updateFoodsTable();

        } catch(SQLException e){ e.printStackTrace(); }
    }


    // ---
    // STATISTICS TAB
    // --------------

    @FXML
    public void loadGeneralStatictics(){
        try{
            // MOST PURCHASED MEAL : DATE
            App.database.prepare("SELECT COUNT(*) as count, date FROM meal, has_meal " +
                    "WHERE meal.mid = has_meal.mid " +
                    "GROUP BY date " +
                    "ORDER BY count DESC");

            Map result = App.database.fetch(null);
            if(result.isEmpty()){
                mostPurchasedDateLb.setText("not present");
            } else{
                mostPurchasedDateLb.setText((String) result.get("date"));
            }

        } catch(SQLException e){ System.out.println("// MOST PURCHASED MEAL : DATE"); e.printStackTrace(); }

            // MOST PURCHASES ON AVERAGE : WEEKDAY


        try{
            // MOST SERVED FOOD : FOOD
            App.database.prepare("SELECT COUNT(*) as count, food_name FROM food, has_food" +
                    "WHERE food.fid=has_food.fid GROUP BY fid ORDER BY count DESC");

            Map result = App.database.fetch(null);
            if(result.isEmpty()){
                mostServedMealLb.setText("not present");
            } else{
                mostServedMealLb.setText((String) result.get("date"));
            }

        } catch(SQLException e){ System.out.println("// MOST SERVED FOOD : FOOD"); e.printStackTrace(); }
    }

    @FXML
    public void loadDateIntervalStatistics(){
        LocalDate startDate, endDate;
        startDate = stat_startDt.getValue();
        endDate = stat_endDt.getValue();

        if(startDate==null || endDate==null)
            return; // both need to be picked to proceed.

        try{
            // STUDENTS PURCHASED ANY MEAL
            App.database.prepare("SELECT COUNT(*) as count FROM has_meal" +
                    "WHERE date>=? AND date<=? GROUP BY pid");

            Map result = App.database.fetch(new HashMap<Integer, Object>(){{
                put(1, startDate);
                put(2, endDate);
            }});

            if(result.isEmpty()){
                mostServedMealLb.setText("N/A");
            } else{
                mostServedMealLb.setText((String) result.get("count"));
            }

        } catch(SQLException e){ e.printStackTrace(); }
        // STUDENTS CHANGED REFECTORY

        // STUDENT MISSED A MEAL THEY PURCHASED

    }

    @FXML
    public void reloadStatistics(){
        loadGeneralStatictics();
        loadDateIntervalStatistics();
    }


    // UPDATING METHODS
    private void updateReservationsTable(){
        try{
            ArrayList<AdminReservation> resList = new ArrayList<>();
            App.database.prepare("SELECT * FROM reservations, meal " +
                    "WHERE reservations.mid=meal.mid");
            List<Map<String, Object>> results = App.database.fetchAll(null);

            for(Map<String, Object> result: results){
                resList.add(new AdminReservation(
                        (String) result.get("pid"),
                        (Integer) result.get("mid"),
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
            ArrayList<AdminPurchase> purchaseList = new ArrayList<>();
            App.database.prepare("SELECT * FROM has_meal, meal " +
                    "WHERE has_meal.mid=meal.mid");
            List<Map<String, Object>> results = App.database.fetchAll(null);

            for(Map<String, Object> result: results){
                purchaseList.add(new AdminPurchase(
                        (String) result.get("pid"),
                        (Integer) result.get("mid"),
                        ((java.sql.Date) result.get("date")).toLocalDate(),
                        (String) result.get("repast"),
                        (String) result.get("refectory")
                ));
            }

            purchaseTb.getItems().setAll(purchaseList);
        } catch(SQLException e){ e.printStackTrace(); }
    }

    private void updateMealsTable(){
        try{
            ArrayList<Meal> mealList = new ArrayList<>();
            App.database.prepare("SELECT mid, date, repast FROM meal");
            List<Map<String, Object>> results = App.database.fetchAll(null);

            for(Map<String, Object> result: results){
                mealList.add(new Meal(
                        (Integer) result.get("mid"),
                        ((java.sql.Date) result.get("date")).toLocalDate(),
                        (String) result.get("repast")
                ));
            }

            mealsTb.getItems().setAll(mealList);
        } catch(SQLException e){ e.printStackTrace(); }
    }

    @FXML
    private void updateFoodsTable(){
        try{
            ArrayList<Food> foodList = new ArrayList<>();
            App.database.prepare("SELECT fid, food_name FROM food");
            List<Map<String, Object>> results = App.database.fetchAll(null);

            for(Map<String, Object> result: results){
                foodList.add(new Food(
                        (Integer) result.get("fid"),
                        (String) result.get("food_name")
                ));
            }

            foodsTb.getItems().setAll(foodList);
            updateFoodFields();
        } catch(SQLException e){ e.printStackTrace(); }
    }

    private void updateFoodFields(){
        ObservableList<Food> foodList = foodsTb.getItems();

        ArrayList<ComboBox<Food>> fieldlist = new ArrayList<>();
        fieldlist.add(meal_foodCb1);
        fieldlist.add(meal_foodCb2);
        fieldlist.add(meal_foodCb3);
        fieldlist.add(meal_foodCb4);
        fieldlist.add(add_foodCb1);
        fieldlist.add(add_foodCb2);
        fieldlist.add(add_foodCb3);
        fieldlist.add(add_foodCb4);

        for(ComboBox<Food> f: fieldlist)
            f.getItems().setAll(foodList);
    }

    private void fetchFoodsOfMeal(){
        Meal selectedMeal = mealsTb.getSelectionModel().getSelectedItem();

        try{
            updateFoodFields();

            App.database.prepare("SELECT food.fid, food_name FROM food, has_food" +
                    " WHERE food.fid=has_food.fid AND mid=?");

            List<Map<String, Object>> results = App.database.fetchAll(new HashMap<Integer, Object>(){{
                put(1, selectedMeal.getMid());
            }});

            meal_foodCb1.getSelectionModel().select(new Food(
                    (Integer) results.get(0).get("fid"),
                    (String) results.get(0).get("food_name")
            ));

            meal_foodCb2.getSelectionModel().select(new Food(
                    (Integer) results.get(1).get("fid"),
                    (String) results.get(1).get("food_name")
            ));

            meal_foodCb3.getSelectionModel().select(new Food(
                    (Integer) results.get(2).get("fid"),
                    (String) results.get(2).get("food_name")
            ));

            meal_foodCb4.getSelectionModel().select(new Food(
                    (Integer) results.get(3).get("fid"),
                    (String) results.get(3).get("food_name")
            ));

        }catch(SQLException e){ e.printStackTrace();}

    }
}
