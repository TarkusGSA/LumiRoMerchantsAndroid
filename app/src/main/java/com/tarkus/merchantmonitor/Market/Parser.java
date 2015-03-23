package com.tarkus.merchantmonitor.Market;

import android.util.Log;
import com.tarkus.merchantmonitor.Item;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by kein on 07/02/14.
 */
public class Parser {
    private static final String TAG = "net.lumiro.parser";

    public static final String SELL_URI = "http://market.lumiro.net/whosell.php?field=price&order=asc&s=";

    private static final String ResultMatchRegExp = "tr\\sclass=\"line(?:[\\s\\S]*?)div\\sstyle=\"([\\s\\S]*?)\"(?:[\\s\\S]*?)<small>([\\d\\+]*)<\\/small>(?:[\\s\\S]*?)javascript:perf\\('([\\d]+)'\\);\">([^<]+?)<\\/a>([\\s\\S]*?)<\\/td>(?:[\\s\\S]*?)class=\"value\">([\\s\\S]*?)class=\"value\"\\salign=\"right\">([^<]+)<(?:[\\s\\S]*?)align=\"center\">([^<]+)(?:[\\s\\S]*?)class=\"trader(?:[\\s\\S]*?)<a[^>]+>([^<]+)";
    //                                              /tr\\sclass=\"line(?:[\\s\\S]*?)div\\sstyle=\"([\\s\\S]*?)\"(?:[\\s\\S]*?)<small>([\\d\\+]*)<\\/small>(?:[\\s\\S]*?)javascript:perf\\('([\\d]+)'\\);\">([^<]+?)<\\/a>([\\s\\S]*?)<\\/td>(?:[\\s\\S]*?)class=\"value\">([\\s\\S]*?)class=\"value\"\\salign=\"right\">([^<]+)<(?:[\\s\\S]*?)align=\"center\">([^<]+)(?:[\\s\\S]*?)class=\"trader(?:[\\s\\S]*?)<a[^>]+>([^<]+)/gim
    private static Pattern parseRegExp;

    public static Pattern getParser(){
        if(parseRegExp == null){
            parseRegExp = Pattern.compile(ResultMatchRegExp, Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
        }
        return parseRegExp;
    }

    public static String getSellItems(String search) {
        String url = SELL_URI;
        try{
            url += URLEncoder.encode(search, "utf-8");
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
            Log.e(TAG, "Get html failed. IOException");
            return null;
        }

        String html = "";
        HttpEntity in = response.getEntity();
        try {
            html = EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            Log.e(TAG, "Read html failed. IOException");
            return null;
        }
        return html;
    }

    public static List<Item> getItems(String search){
        String response = getSellItems(search);
        if(response == null){
            return null;
        }

        List<Item> items = new ArrayList<Item>();
        Matcher m = getParser().matcher(response);


        while(m.find()) {
            // Получаю карты и доп описание
            String tmp_cards = "";
            String aditionalsString = m.group(6).trim();
            String creator = "";
            if(aditionalsString.length()>0){
                String cardsRegExp = "javascript:perf\\('[^']+'\\);\">([^<]+?)<\\/a>";
                Pattern cardPattern = Pattern.compile(cardsRegExp, Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
                Matcher cardMatcher = cardPattern.matcher(aditionalsString);
                while( cardMatcher.find()  ){
                    tmp_cards += cardMatcher.group(1);
                }

                String creatorRegExp = "javascript:perf\\('[^']+'\\);\">([^<]+?)<\\/a>'s";
                cardPattern = Pattern.compile(creatorRegExp, Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
                cardMatcher = cardPattern.matcher(aditionalsString);
                if( cardMatcher.find() ) {
                    creator = cardMatcher.group(1) + " ";
                    tmp_cards = tmp_cards.replace(cardMatcher.group(1).trim(),"");
                }
            }

            Item item = new Item();
            item.setId(Integer.parseInt(m.group(3)));
            Integer refain = 0;
            try {
                refain = Integer.parseInt(m.group(2).replace("+",""));
            } catch(NumberFormatException e) {}
            item.setRefain( refain );
            item.setPrice(Integer.parseInt(m.group(7).replace(".","")));
            item.setCount(Integer.parseInt(m.group(8)));
            item.setNow_count(item.getCount());

            String itemName = creator+m.group(4);
            // Получаю количество слотов
            if (m.group(5).indexOf(']')>=0)
            {
                itemName = itemName+" "+m.group(5).substring(0,m.group(5).indexOf(']')+1).trim();
            }
            item.setName(itemName);
//            Log.d(TAG, "name - "+m.group(5)+" || "+m.group(6));
            item.setAttr(tmp_cards);
            item.setOwner(m.group(9));
            items.add(item);
        }
        Log.d(TAG, "Items count: " + String.valueOf(items.size()));
        return items;
    }
}
