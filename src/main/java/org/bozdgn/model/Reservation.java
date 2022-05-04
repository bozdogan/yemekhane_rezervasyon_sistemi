package org.bozdgn.model;

import java.time.LocalDate;

public class Reservation {
    /** Only valid if positive */
    int mid;
    LocalDate date;
    String repast;
    String refectory;

    public Reservation(LocalDate date, String repast, String refectory) {
        this.mid = -1;
        this.date = date;
        this.repast = repast;
        this.refectory = refectory;
    }

    public Reservation(int mid, LocalDate date, String repast, String refectory) {
        this.mid = mid;
        this.date = date;
        this.repast = repast;
        this.refectory = refectory;
    }

    public int getMid() {
        return mid >= 0
                ? mid
                : -1;
    }


    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getRepast() {
        return repast;
    }

    public void setRepast(String repast) {
        this.repast = repast;
    }

    public String getRefectory() {
        return refectory;
    }

    public void setRefectory(String refectory) {
        this.refectory = refectory;
    }
}
