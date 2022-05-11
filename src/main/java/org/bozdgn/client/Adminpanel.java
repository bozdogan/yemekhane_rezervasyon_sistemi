package org.bozdgn.client;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

import javafx.scene.control.cell.PropertyValueFactory;
import org.bozdgn.model.Food;
import org.bozdgn.model.Meal;
import org.bozdgn.model.Reservation;
import org.bozdgn.model.ReservedMeal;
import org.bozdgn.service.FoodService;
import org.bozdgn.service.MealService;
import org.bozdgn.service.ReservationService;
import org.bozdgn.service.StatisticsService;

public class Adminpanel implements Initializable{

    @FXML private TableView<ReservedMeal> reservationTb;
    @FXML private TableColumn<ReservedMeal, String> reservation_pid;
    @FXML private TableColumn<ReservedMeal, Integer> reservation_mid;
    @FXML private TableColumn<ReservedMeal, LocalDate> reservation_date;
    @FXML private TableColumn<ReservedMeal, String> reservation_repast;
    @FXML private TableColumn<ReservedMeal, String> reservation_refectory;

    @FXML private Button man_cancelResBt;
    @FXML private Button man_purchaseBt;
    @FXML private Button man_removeResBt;

    @FXML private TableView<ReservedMeal> purchaseTb;
    @FXML private TableColumn<ReservedMeal, String> purchase_pid;
    @FXML private TableColumn<ReservedMeal, Integer> purchase_mid;
    @FXML private TableColumn<ReservedMeal, LocalDate> purchase_date;
    @FXML private TableColumn<ReservedMeal, String> purchase_repast;
    @FXML private TableColumn<ReservedMeal, String> purchase_refectory;

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
        ObservableList<ReservedMeal> selectedRes = reservationTb.getSelectionModel().getSelectedItems();

        if(selectedRes.size()==0){
            AlertBox.showWarning("No reservations selected.");
            return;
        }

        String confirmationMessage = selectedRes.size()+" reservations selected. " +
                "These reservations will be cancelled.\n\nProceed?";

        if(AlertBox.showConfirmation("Confirmation", confirmationMessage)){
            List<Reservation> meals = new ArrayList<>(selectedRes.size());
            for(ReservedMeal r: selectedRes){
                // NOTE(bora): `refectory` is not needed to delete.
                meals.add(new org.bozdgn.model.Reservation(
                        r.getPid(),
                        r.getMid(),
                        null));
            }

            ReservationService.batchCancelReservationsByIDs(App.database, meals);
            updateReservationsTable();
        }
    }

    @FXML
    public void setReservationsPurchased(){
        ObservableList<ReservedMeal> selectedRes = reservationTb.getSelectionModel().getSelectedItems();
        int numberOfSelectedItems = selectedRes.size();

        if(numberOfSelectedItems==0){
            AlertBox.showWarning("No reservations selected.");
            return;
        }

        String confirmationMessage = String.format("%d reservations will be marked as purchased." +
                "\n\nProceed?", numberOfSelectedItems);
        if(AlertBox.showConfirmation("Confirmation", confirmationMessage)){
            for(ReservedMeal it: selectedRes) {
                ReservationService.makePurchase(App.database, it.getPid(), it.getMid());
            }

            updateReservationsTable();
            updatePurchaseTable();
        }
    }

    @FXML
    public void changeRefectory(){
        ObservableList<ReservedMeal> selectedPurchases = purchaseTb.getSelectionModel().getSelectedItems();
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
            List<Reservation> meals = new ArrayList<>(selectedPurchases.size());
            for(ReservedMeal r: selectedPurchases){
                // NOTE(bora): `refectory` is not needed to delete.
                meals.add(new Reservation(
                        r.getPid(),
                        r.getMid(),
                        null));
            }

            ReservationService.batchChangeReservationRefectory(App.database, meals, newRefectory);
            updateReservationsTable();
        }
    }

    @FXML
    public void removePurchases(){
        ObservableList<ReservedMeal> selectedPurchases = purchaseTb.getSelectionModel().getSelectedItems();
        int numberOfSelectedItems = selectedPurchases.size();

        if(numberOfSelectedItems==0){
            AlertBox.showWarning("No purchases selected.");
            return;
        }

        String confirmationMessage = String.format("%d purchases will be removed." +
                "\n\nProceed?", numberOfSelectedItems);
        if(AlertBox.showConfirmation("Confirmation", confirmationMessage)){
            List<Reservation> meals = new ArrayList<>(selectedPurchases.size());
            for(ReservedMeal r: selectedPurchases){
                // NOTE(bora): `refectory` is not needed to delete.
                meals.add(new org.bozdgn.model.Reservation(
                        r.getPid(),
                        r.getMid(),
                        null));
            }

            ReservationService.batchRemovePurchaseByIDs(App.database, meals);
            updatePurchaseTable();
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
            MealService.removeMealByID(App.database, selectedMeal.getMid());
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

        int food1ID = add_foodCb1.getSelectionModel().getSelectedItem().getFid();
        int food2ID = add_foodCb2.getSelectionModel().getSelectedItem().getFid();
        int food3ID = add_foodCb3.getSelectionModel().getSelectedItem().getFid();
        int food4ID = add_foodCb4.getSelectionModel().getSelectedItem().getFid();


        MealService.addMeal(App.database, mealDate, repastStr);
        int mealID = MealService.getMealID(App.database, mealDate, repastStr);

        MealService.addMealFood(App.database, mealID, food1ID);
        MealService.addMealFood(App.database, mealID, food2ID);
        MealService.addMealFood(App.database, mealID, food3ID);
        MealService.addMealFood(App.database, mealID, food4ID);

        updateMealsTable();
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
            for(Food it: selectedFoods) {
                FoodService.removeFood(App.database, it.getFid());
            }

            updateFoodsTable();
        }}

    @FXML
    public void addFood(){
        String foodName = foodNameTx.getText();

        if(foodName.equals(""))
            return;

        FoodService.addFood(App.database, foodName);
    }


    // ---
    // STATISTICS TAB
    // --------------

    @FXML
    public void loadGeneralStatictics(){

        // MOST PURCHASED MEAL : DATE
        LocalDate mostPurchasedDate = StatisticsService.getMostPurchasedDate(App.database);
        mostPurchasedDateLb.setText(mostPurchasedDate.toString());

        // MOST PURCHASES ON AVERAGE : WEEKDAY


        // MOST SERVED FOOD : FOOD
        mostServedMealLb.setText(
                StatisticsService.getMostServedFood(App.database));
    }

    @FXML
    public void loadDateIntervalStatistics(){
        LocalDate startDate, endDate;
        startDate = stat_startDt.getValue();
        endDate = stat_endDt.getValue();

        if(startDate==null || endDate==null)
            return; // both need to be picked to proceed.

        int count = StatisticsService.countPurchasedMeals(
                App.database,
                startDate,
                endDate);

        mostServedMealLb.setText(String.valueOf(count));


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
        List<ReservedMeal> resList = ReservationService.listUnpaidAll(App.database);
        reservationTb.getItems().setAll(resList);
    }

    private void updatePurchaseTable(){
        List<ReservedMeal> purchaseList = ReservationService.listPaidAll(App.database);
        purchaseTb.getItems().setAll(purchaseList);
    }

    private void updateMealsTable(){
        List<Meal> mealList = MealService.listMeals(App.database);
        mealsTb.getItems().setAll(mealList);
    }

    @FXML
    private void updateFoodsTable(){
        List<Food> foodList = FoodService.listFoods(App.database);
        foodsTb.getItems().setAll(foodList);
        updateFoodFields();
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

        updateFoodFields();

        List<Food> foods = MealService.getFoods(App.database, selectedMeal.getMid());
        meal_foodCb1.getSelectionModel().select(foods.get(0));
        meal_foodCb2.getSelectionModel().select(foods.get(2));
        meal_foodCb3.getSelectionModel().select(foods.get(3));
        meal_foodCb4.getSelectionModel().select(foods.get(4));
    }
}
