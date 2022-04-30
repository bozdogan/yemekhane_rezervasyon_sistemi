package org.bozdgn.client.data;

import java.time.LocalDate;

public class Purchase{
    private LocalDate date;
    private String repast;
    private String refectory;

    public Purchase(LocalDate date, String repast, String refectory){
        this.date = date;
        this.repast = repast;
        this.refectory = refectory;
    }

    public LocalDate getDate(){ return date; }
    public void setDate(LocalDate date){ this.date = date; }

    public String getRepast(){ return repast; }
    public void setRepast(String repast){ this.repast = repast; }

    public String getRefectory(){ return refectory; }
    public void setRefectory(String refectory){ this.refectory = refectory; }
}
