package com.tarkus.merchantmonitor.db;

import com.tarkus.merchantmonitor.Item;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kein on 09/02/14.
 */
public class ItemAdapter {

    public static List<Item> decodeJSONItems(String json){
        List<Item> items = new ArrayList<Item>();
        try {
            JSONArray json_items = new JSONArray(json);
            for(Integer i=0; i< json_items.length(); i++){
                Item item = new Item();
                JSONObject jitem = (JSONObject)json_items.get(i);
                item.setId( jitem.getInt("id") );
                item.setName(jitem.getString("name"));
                item.setRefain(jitem.getInt("refain"));
                item.setPrice(jitem.getInt("price"));
                item.setCount(jitem.getInt("count"));
                item.setNow_count(jitem.getInt("now_count"));
                item.setAttr(jitem.getString("attr"));
                item.setOwner(jitem.getString("owner"));
                items.add(item);
            }
        } catch (JSONException e) {
        }
        return items;
    }

    public static String encodeJSONItems(List<Item> items){
        JSONArray jitems = new JSONArray();
        for(Item item: items){
            JSONObject jitem = new JSONObject();
            try {
                jitem.put("id", item.getId());
                jitem.put("name", item.getName());
                jitem.put("refain", item.getRefain());
                jitem.put("price", item.getPrice());
                jitem.put("count", item.getCount());
                jitem.put("now_count", item.getNow_count());
                jitem.put("attr", item.getAttr());
                jitem.put("owner", item.getOwner());
                jitems.put(jitem);
            } catch (JSONException e) {}
        }
        return jitems.toString();
    }
}
