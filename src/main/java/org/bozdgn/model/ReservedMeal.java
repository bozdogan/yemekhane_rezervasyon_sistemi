package org.bozdgn.model;

import java.time.LocalDate;

/** A view model for table view widgets of client UI. */
public class ReservedMeal {
    String pid;
    int mid;
    LocalDate date;
    String repast;
    String refectory;

    boolean _is_complete_view;


    public ReservedMeal(LocalDate date, String repast, String refectory) {
        this.pid = null;
        this.mid = -1;
        _is_complete_view = false;

        this.date = date;
        this.repast = repast;
        this.refectory = refectory;
    }

    public ReservedMeal(String pid, int mid, LocalDate date, String repast, String refectory) {
        this.mid = mid;
        this.date = date;
        this.repast = repast;
        this.refectory = refectory;
    }


    public String getPid() {
        return pid;
    }

    public int getMid() {
        return mid >= 0
                ? mid
                : -1;
    }

    /** If this model is used on user panel, then it doesn't have to have
     * `pid` and `mid` fields. This flag specifies if those fields are valid. */
    public boolean isCompleteView() {
        return _is_complete_view;
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
