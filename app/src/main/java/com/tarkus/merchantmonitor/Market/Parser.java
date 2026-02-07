package com.tarkus.merchantmonitor.Market;

import android.util.Log;
import com.tarkus.merchantmonitor.Item;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kein on 07/02/14.
 */
public class Parser {
    private static final String TAG = "net.lumiro.parser";

    public static final String SELL_URI = "https://lumi-ragnarok.net/api/market/whosell?limit=50&offset=0&filter=etc&query=";

    public static String getSellItems(String search) {
        String url = SELL_URI;
        try{
            url += URLEncoder.encode(search, "utf-8");
            url += "&jobs=all";
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Search string encodeURI fail. "+search);
            return null;
        }
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(url);
        HttpResponse response = null;
        try {
            Log.d(TAG, "Requesting url:" + url);
            response = client.execute(request);
        } catch (IOException e) {
            Log.e(TAG, "Get json failed. IOException");
            return null;
        }

        try {
            return EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            Log.e(TAG, "Read json failed. IOException");
            return null;
        }
    }

    public static List<Item> getItems(String search){
        String response = getSellItems(search);
        if(response == null){
            return null;
        }

        List<Item> items = new ArrayList<Item>();

        try {
            JSONObject root = new JSONObject(response);
            JSONArray results = root.optJSONArray("results");
            if (results == null) {
                Log.e(TAG, "No results in response");
                return items;
            }

            for (int i = 0; i < results.length(); i++) {
                JSONObject result = results.optJSONObject(i);
                if (result == null) {
                    continue;
                }

                String owner = result.optString("name", "");
                if (!search.equals(owner)) {
                    continue;
                }

                JSONObject itemObj = result.optJSONObject("item");
                if (itemObj == null) {
                    continue;
                }

                Item item = new Item();
                item.setId(itemObj.optInt("id", 0));
                item.setRefain(result.optInt("refine", 0));
                item.setPrice(result.optInt("price", 0));
                int amount = result.optInt("amount", 0);
                item.setCount(amount);
                item.setNow_count(amount);

                String itemName = itemObj.optString("name_japanese", "");
                int slots = itemObj.optInt("slots", 0);
                if (slots > 0) {
                    itemName = itemName + " [" + slots + "]";
                }
                item.setName(itemName);

                item.setAttr(buildCardsString(result));
                item.setOwner(owner);

                items.add(item);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Parse json failed. JSONException");
            return null;
        }

        Log.d(TAG, "Items count: " + String.valueOf(items.size()));
        return items;
    }

    private static String buildCardsString(JSONObject result) {
        StringBuilder cards = new StringBuilder();
        appendCardName(cards, result, "card_1");
        appendCardName(cards, result, "card_2");
        appendCardName(cards, result, "card_3");
        appendCardName(cards, result, "card_4");
        return cards.toString().trim();
    }

    private static void appendCardName(StringBuilder cards, JSONObject result, String key) {
        if (!result.has(key) || result.isNull(key)) {
            return;
        }

        String cardName = "";
        try {
            Object card = result.get(key);
            if (card instanceof JSONObject) {
                cardName = ((JSONObject) card).optString("name_japanese", "");
            } else {
                cardName = String.valueOf(card);
            }
        } catch (JSONException e) {
            return;
        }

        if (cardName.length() == 0) {
            return;
        }

        if (cards.length() > 0) {
            cards.append(" ");
        }
        cards.append(cardName);
    }
}
