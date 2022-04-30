package org.bozdgn.client.data;

public class Food{
    private int fid;
    private String foodName;

    public Food(int fid, String food_name){
        this.fid = fid;
        this.foodName = food_name;
    }

    public int getFid(){ return fid; }
    public void setFid(int fid){ this.fid = fid; }

    public String getFoodName(){ return foodName; }
    public void setFoodName(String foodName){ this.foodName = foodName; }

    @Override
    public String toString(){ return foodName; }

    @Override
    public boolean equals(Object obj){
        return obj instanceof Food && ((Food) obj).fid==this.fid;
    }

    @Override
    public int hashCode(){ return fid*17; }
}
