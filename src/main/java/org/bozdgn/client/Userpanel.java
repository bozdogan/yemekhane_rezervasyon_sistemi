package org.bozdgn.client;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import javafx.scene.control.cell.PropertyValueFactory;
import org.bozdgn.model.Reservation;
import org.bozdgn.service.MealService;
import org.bozdgn.service.ReservationService;

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

    @FXML private TableView<Reservation> purchaseTb;
    @FXML private TableColumn<Reservation, LocalDate> purchase_date;
    @FXML private TableColumn<Reservation, String> purchase_repast;
    @FXML private TableColumn<Reservation, String> purchase_refectory;

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
    public void addReservation() {
        LocalDate resDate = reservationDate.getValue();

        String repastStr;
        RadioButton selectedRepast = (RadioButton) repast.getSelectedToggle();
        if(selectedRepast == repastB) repastStr = "B";
        else if(selectedRepast == repastL) repastStr = "L";
        else if(selectedRepast == repastD) repastStr = "D";
        else repastStr = null;

        String refectoryStr;
        RadioButton selectedRefectory = (RadioButton) refectory.getSelectedToggle();
        if(selectedRefectory == refectoryIe) refectoryStr = "ieylul";
        else if(selectedRefectory == refectoryYe) refectoryStr = "yemre";
        else refectoryStr = null;

        if(resDate == null || selectedRepast == null || selectedRefectory == null) {
            AlertBox.showWarning("Please fill all the fields.");
            return;
        }

        // OBTAIN MEAL ID
        int meal_id = MealService.getMealID(App.database, resDate, repastStr);
        if(meal_id == -1) { // if meal d.n.e, meal id cannot be obtained.
            AlertBox.showWarning("No meal available with specified date and repast.");
            return;
        }

        // CHECK IF ALREADY RESERVED OR PURCHASED
        boolean already_on_the_list = ReservationService.isReserved(App.database, App.personId, resDate, repastStr);

        if(already_on_the_list) {
            AlertBox.showWarning("Meal already reserved or purchased:\n" +
                    "    " + resDate + " " + repastStr);
            return;
        }

        // ADD RESERVATION ENTRY
        int affected = ReservationService.makeReservation(App.database,
                App.personId,
                meal_id,
                refectoryStr);

        if(affected > 0)
            updateReservationsTable();
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
            List<Reservation> meals = new ArrayList<>(selectedRes.size());
            for(Reservation r: selectedRes){
                meals.add(new Reservation(r.getDate(), r.getRepast(), null));
            }

            ReservationService.cancelReservations(App.database, App.personId, meals);
            updateReservationsTable();
        }
    }

    @FXML
    public void purchaseReservations(){
        // GET PRICE
        long resCount = ReservationService.countUnpaid(App.database, App.personId);
        String confirmationMessage = String.format("%d reservations has total price of\n"+
                "    ₺%.2f\n\nProceed?", resCount, resCount*App.unitPrice);

        if(AlertBox.showConfirmation("Confirmation", confirmationMessage)){

            for(Reservation it: ReservationService.listUnpaid(App.database, App.personId)) {
                ReservationService.makeSinglePurchase(App.database, App.personId, it.getMid());
            }

            updateReservationsTable();
            updatePurchaseTable();
        }
    }

    @FXML
    public void changeRefectory(){
        Reservation selected = purchaseTb.getSelectionModel().getSelectedItem();

        if(selected == null){
            AlertBox.showWarning("No purchases selected.");
            return;
        }

        String newRefectory = selected.getRefectory().equals("ieylul") ? "yemre" : "ieylul";
        String confirmationMessage = String.format("Current refectory is %s, you will change it with:\n"+
                "    %s\n\nProceed?", selected.getRefectory(), newRefectory);

        if(AlertBox.showConfirmation("Confirmation", confirmationMessage)){

            int mid = MealService.getMealID(App.database, selected.getDate(), selected.getRepast());
            ReservationService.changeReservationRefectory(App.database, App.personId, mid, newRefectory);

                updatePurchaseTable();
        }
    }

    private void updateReservationsTable(){
        // load the items
        List<Reservation> resList = ReservationService.listUnpaid(App.database, App.personId);
        reservationTb.getItems().setAll(resList);
    }

    private void updatePurchaseTable(){
        // load the items
        List<Reservation> purchaseList = ReservationService.listPaid(App.database, App.personId);
        purchaseTb.getItems().setAll(purchaseList);
    }
}
