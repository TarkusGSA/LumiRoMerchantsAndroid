package com.tarkus.merchantmonitor.helper;

import com.tarkus.merchantmonitor.Item;
import com.tarkus.merchantmonitor.Merc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kein on 09/02/14.
 */
public class MerchantUpdater {

    public final static String TAG = "net.tarkus.market.market_updater";

    private boolean is_new = false;

    private boolean is_offline = false;

    private Integer profit = 0;

    private Merc merc;


    public MerchantUpdater(Merc merc) {
        this.merc = merc;
    }

    public List<Item> proceed_new_items(List<Item> new_items, List<Item> old_items){
        // Merchant offline
        if(0 == new_items.size()){
            is_offline = true;
            return old_items;//new_items; // Попробую не выводить торговца офлайн никогда
        }

        // Merchant online
        if( 0 == old_items.size()){
            is_new = true;
            return new_items;
        }

        ArrayList<String> old_items_checked = new ArrayList<String>(), new_items_checked = new ArrayList<String>();
        String old_hash, new_hash;
        // Cycles for updating new count of items
        for(int i = 0; i<old_items.size(); i++){
            old_hash = item_hash(old_items.get(i));
            for(int j=0; j<new_items.size(); j++){
                new_hash = item_hash(new_items.get(j));
                if(!old_items_checked.contains(old_hash+String.valueOf(i))
                        && !new_items_checked.contains(new_hash+String.valueOf(j))
                        && old_hash.equals(new_hash)
                        ){
                    if(new_items.get(j).getCount()
                            > old_items.get(i).getNow_count()){
                        is_new = true;
                        return new_items;
                    }
                    old_items_checked.add(old_hash+String.valueOf(i));
                    new_items_checked.add(new_hash+String.valueOf(j));
                    old_items.get(i).setNow_count(new_items.get(j).getCount());
                }
            }
        }

        // Now set now_count=0 for items that not found in new items list
        for(int i=0; i<old_items.size(); i++){
            old_hash = item_hash(old_items.get(i));
            if(!old_items_checked.contains(old_hash+String.valueOf(i))){
                old_items.get(i).setNow_count(0);
            }
        }

        // Check is new vend? Does we have new items?
        boolean old_item_found = false;
        for(int i=0; i<new_items.size(); i++){
            new_hash = item_hash(new_items.get(i));
            for(int j=0; j<old_items.size(); j++){
                old_item_found = false;
                old_hash = item_hash(old_items.get(j));
                if(new_hash.equals(old_hash)){
                    old_item_found = true;
                    break;
                }
            }
            if(!old_item_found){
                is_new = true;
                old_items = new_items;
                break;
            }
        }
        return old_items;
    }

    public Integer getProfit() {
        return profit;
    }

    public void updateItems(List<Item> new_items){
        List<Item> old_items = merc.getItems();
        profit = -get_profit(old_items);

        List<Item> summary_items = proceed_new_items(new_items, old_items);

        profit +=  get_profit(summary_items);
        merc.setItems(summary_items);
    }

    private Integer get_profit(List<Item> items){
        Integer profit = 0;
        for(Item item : items){
            profit += item.getProfit();
        }
        return profit;
    }

    public boolean isOffline() {
        return is_offline;
    }

    public boolean isNew() {
        return is_new;
    }

    private static String item_hash(Item item){
        // item.id+item.refain+item.price+item.attr.join('|');
        return String.valueOf(item.getId()) + "|"
                + String.valueOf(item.getRefain()) + "|"
                + String.valueOf(item.getPrice()) + "|"
                + String.valueOf(item.getAttr()) + "|"
                ;
    }
}
