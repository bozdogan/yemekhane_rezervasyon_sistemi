package org.bozdgn.model;

import java.time.LocalDate;

public class AdminPurchase{
    private String pid;
    private int mid;
    private LocalDate date;
    private String repast;
    private String refectory;

    public AdminPurchase(String pid, int mid, LocalDate date, String repast, String refectory){
        this.pid = pid;
        this.mid = mid;
        this.date = date;
        this.repast = repast;
        this.refectory = refectory;
    }

    public String getPid(){ return pid; }
    public void setPid(String pid){ this.pid = pid; }

    public int getMid(){ return mid; }
    public void setPid(int pid){ this.mid = mid; }

    public LocalDate getDate(){ return date; }
    public void setDate(LocalDate date){ this.date = date; }

    public String getRepast(){ return repast; }
    public void setRepast(String repast){ this.repast = repast; }

    public String getRefectory(){ return refectory; }
    public void setRefectory(String refectory){ this.refectory = refectory; }
}
