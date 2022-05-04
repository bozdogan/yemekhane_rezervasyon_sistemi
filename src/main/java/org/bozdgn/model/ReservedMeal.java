package org.bozdgn.model;

import java.time.LocalDate;

public class ReservedMeal {
    LocalDate date;
    String repast;
    String refectory;

    public ReservedMeal( LocalDate date, String repast, String refectory) {
        this.date = date;
        this.repast = repast;
        this.refectory = refectory;
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
