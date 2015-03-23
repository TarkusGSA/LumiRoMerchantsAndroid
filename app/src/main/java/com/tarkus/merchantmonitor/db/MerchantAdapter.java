package com.tarkus.merchantmonitor.db;

import com.tarkus.merchantmonitor.Item;
import org.json.JSONArray;

import java.util.List;

/**
 * Created by kein on 09/02/14.
 */
public class MerchantAdapter {

    public static String encodeItemsJSON(List<Item> items){
        JSONArray jitems = new JSONArray();
        for(Item item : items){
            jitems.put(item);
        }
        return jitems.toString();
    }
}
