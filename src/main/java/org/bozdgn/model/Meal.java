package org.bozdgn.model;

import java.time.LocalDate;

public class Meal{
    private int mid;
    private LocalDate date;
    private String repast;

    public Meal(int mid, LocalDate date, String repast){
        this.mid = mid;
        this.date = date;
        this.repast = repast;
    }

    public int getMid(){ return mid; }
    public void setMid(int mid){ this.mid = mid; }

    public LocalDate getDate(){ return date; }
    public void setDate(LocalDate date){ this.date = date; }

    public String getRepast(){ return repast; }
    public void setRepast(String repast){ this.repast = repast; }
}
