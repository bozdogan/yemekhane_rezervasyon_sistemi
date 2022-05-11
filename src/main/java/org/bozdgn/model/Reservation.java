package org.bozdgn.model;

public class Reservation {
    String pid;
    int mid;
    String refectory;

    public Reservation(String pid, int mid, String refectory) {
        this.pid = pid;
        this.mid = mid;
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

    public String getRefectory() {
        return refectory;
    }

    public void setRefectory(String refectory) {
        this.refectory = refectory;
    }
}
