package com.tarkus.merchantmonitor;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.text.format.DateUtils;
import android.util.Log;

import com.tarkus.merchantmonitor.Market.Parser;
import com.tarkus.merchantmonitor.db.DBAdaptor;
import com.tarkus.merchantmonitor.helper.MerchantUpdater;

import android.text.format.Time;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kein on 06/02/14.
 */
public class Market_Service extends Service implements Runnable {
    // Update time interval
    private long UPDATE_INTERVAL = 20 * DateUtils.MINUTE_IN_MILLIS;
    // Show notification
    private boolean ShowNotify = true;


//    private static final String TAG = "net.lumiro.market.market_service";
    private static final String TAG = "lumiro";

    public List<String> merchants = new ArrayList<String>();

    private boolean looped = false;

    public static final String ACTION_DEL_MERC = "com.tarkus.merchantmonitor.DEL_MERC";
    public static final String ACTION_SYNC_MERC = "com.tarkus.merchantmonitor.SYNC_MERC";
    public static final String ACTION_SYNC_MERCS = "com.tarkus.merchantmonitor.SYNC_MERCS";
    public static final String ACTION_SYNC_START = "com.tarkus.merchantmonitor.SYNC_START";

    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "Start service");

        updatePreference();
    }

    public void updatePreference()
    {
        // Загружаю из настроек время обновления
        SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(this);
        String updateTimeString = sPref.getString("update_time_list", "20");
        UPDATE_INTERVAL = Integer.parseInt(updateTimeString) * DateUtils.MINUTE_IN_MILLIS;
        Log.e(TAG, "Load Pref Update Time "+updateTimeString);
        // Read notification flag from  settings
        ShowNotify = sPref.getBoolean("notif",true);
        Log.e(TAG, "Load Pref Notif "+String.valueOf(ShowNotify));
    }

    public int onStartCommand (Intent intent, int flags, int startId){
        //????
        if (intent==null)
        {
            return START_STICKY;
        }

        Log.e(TAG, intent.getAction());

        updatePreference();

        if (ACTION_DEL_MERC.equals(intent.getAction())) {
            Log.e(TAG, "Delete merc ");
            merchants.remove(intent.getStringExtra("merc_name"));
        }

        if (ACTION_SYNC_MERC.equals(intent.getAction())) {
            Log.e(TAG, "Start merc update");
            merchants.add( intent.getStringExtra("merc_name") );

            Thread t = new Thread(this);
            t.start();
        }
        if (ACTION_SYNC_MERCS.equals(intent.getAction())) {
            Log.e(TAG, "Start update");
            merchants = new ArrayList<String>();
            Thread t = new Thread(this);
            t.start();
        }
        if (ACTION_SYNC_START.equals(intent.getAction())) {
            Log.e(TAG, "Start loop");
            merchants = new ArrayList<String>();
            looped = true;
            Thread t = new Thread(this);
            t.start();
        }
        return START_STICKY;
    }

    @Override
    public void run() {
        if(looped){
            Log.e(TAG, "Add event loop");
            Intent i = new Intent(this, Market_Service.class);
            i.setAction(Market_Service.ACTION_SYNC_START);

            PendingIntent pendingIntent = PendingIntent.getService(this, 0, i, PendingIntent.FLAG_IMMUTABLE);

            AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
            Time time = new Time();
            time.set(System.currentTimeMillis() + UPDATE_INTERVAL);

            long nextUpdate = time.toMillis(false);
            alarmManager.set(AlarmManager.RTC_WAKEUP, nextUpdate, pendingIntent);
        }
        if(0 == merchants.size()){
            DBAdaptor db = new DBAdaptor(this);
            List<Merc> mersc = db.getMercs();
            try {
                for (Merc merc : mersc) {
                    merchants.add(merc.getName());
                }
            } finally {
                db.close();
            }
        }
        Log.d(TAG, "Service start sync");
        for(String merchant_name : merchants){
            Log.d(TAG, "Requested SYNC_MERC action ["+merchant_name+"]");
            DBAdaptor db = new DBAdaptor(this);
            List<Item> items = Parser.getItems(merchant_name);
            if(null == items){
                continue;
            }
            try {
                Merc merc = db.getMercByName(merchant_name);

                MerchantUpdater updater = new MerchantUpdater(merc);
                updater.updateItems(items);

                if (updater.isNew() && ShowNotify) {
                    Intent RTReturn = new Intent(MainActivity.SHOW_TOAST);
                    RTReturn.putExtra("toast", merc.getName() + " online ");
                    LocalBroadcastManager.getInstance(this).sendBroadcast(RTReturn);
                }
                Log.d(TAG, "Profit " + merc.getName() + " +" + String.valueOf(updater.getProfit()));
                if (updater.getProfit() > 0 && ShowNotify) {
                    Intent RTReturn = new Intent(MainActivity.SHOW_TOAST);
                    RTReturn.putExtra("toast", merc.getName() + " +" + String.valueOf(updater.getProfit()));
                    LocalBroadcastManager.getInstance(this).sendBroadcast(RTReturn);
                }

                db.updateMerc(merc);
                db.getMercByName(merchant_name);
            } finally {
                db.close();
            }
        }
        Intent RTReturn = new Intent(MainActivity.UPDATE_MERC_LIST);
        LocalBroadcastManager.getInstance(this).sendBroadcast(RTReturn);
    }
}
