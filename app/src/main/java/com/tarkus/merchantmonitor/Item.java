package com.tarkus.merchantmonitor;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by kein on 04/02/14.
 */
public class Item {
    private Integer id;
    private String name;
    private Integer refain;
    private Integer price;
    private Integer count;
    private Integer now_count;
    private String attr;
    private String owner;

    public String encodeJson(){
        try {
            JSONObject item = new JSONObject();
            item.put("id", this.id );
            item.put("name", this.name );
            item.put("refain", this.refain );
            item.put("price", this.price );
            item.put("count", this.count );
            item.put("now_count", this.now_count );
            item.put("attr", this.attr );
            item.put("owner", this.owner );
            return item.toString();
        } catch (JSONException e) {
//            e.printStackTrace();
            return "{}";
        }
    }

    public String toString(){
        return encodeJson();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getRefain() {
        return refain;
    }

    public void setRefain(Integer refain) {
        this.refain = refain;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public Integer getProfit() {
        return (getCount() - getNow_count()) * getPrice();
    }


    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Integer getNow_count() {
        return now_count;
    }

    public void setNow_count(Integer now_count) {
        this.now_count = now_count;
    }

    public String getAttr() {
        return attr;
    }

    public void setAttr(String attr) {
        this.attr = attr;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
}
