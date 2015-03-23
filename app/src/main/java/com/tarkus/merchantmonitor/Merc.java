package com.tarkus.merchantmonitor;

import java.util.*;

/**
 * Created by kein on 04/02/14.
 */
public class Merc {
    private List<Item> items;
    private String name;

    public Merc() {
        this.items = new ArrayList<Item>();
    }

    public void addItem(Item item){
        this.items.add(item);
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toString(){
        return getName() + "(" + String.valueOf(getItems().size()) + ")";
    }

    public Integer getItemsCount(){
/*        Integer items_count = 0;
        for(Item item : getItems()){
            if(item.getNow_count() > 0)
                items_count++;
        }*/
        return items.size(); //items_count;
    }

    public Integer getProfit(){
        Integer profit = 0;
        for(Item item : getItems()){
            profit += item.getProfit();
        }
        return profit;
    }
}
