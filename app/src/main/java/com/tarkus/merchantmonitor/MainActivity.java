package com.tarkus.merchantmonitor;

import android.app.*;
import android.appwidget.AppWidgetManager;
import android.content.*;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ExpandableListView;

import android.widget.RemoteViews;
import android.widget.Toast;
import com.tarkus.merchantmonitor.db.DBAdaptor;
import com.tarkus.merchantmonitor.view.MerchantArrayAdapter;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.prefs.Preferences;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "lumiro";
    private int numMessages = 0;
    private Uri alarmSound;
    private NotificationManager mNotificationManager;
    private int notificationID = 100;

    public static final String EXTRA_MERCH = "com.tarkus.merchantmonitor.MERC_NAME";

    private SharedPreferences sPref;

    public static Item onWidgetItem;


    ExpandableListView mercListView;

    MerchantArrayAdapter adapter;

    public static final String UPDATE_MERC_LIST = "com.com.tarkus.merchantmonitor.UPDATE_MERC_LIST";
    public static final String SHOW_TOAST = "com.com.tarkus.merchantmonitor.SHOW_TOAST";


    private void updateWidget()
    {
        onWidgetItem = getItemToWidget();

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this
                .getApplicationContext());

        ComponentName thisWidget = new ComponentName(getApplicationContext(),
                LumiroWidget.class);

        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

        for (int widgetId : allWidgetIds) {
            // create some random data

            RemoteViews remoteViews = new RemoteViews(this
                    .getApplicationContext().getPackageName(),
                    R.layout.main_widget);

            // Set the text

            if (onWidgetItem==null)
            {
                remoteViews.setTextViewText(R.id.itemName, "Не задан");
                remoteViews.setTextViewText(R.id.itemCnt, "");

                remoteViews.setTextViewText(R.id.itemProfit, "");
                remoteViews.setImageViewResource(R.id.itemImage, R.drawable.item_5819);
            }
            else {

                remoteViews.setTextViewText(R.id.itemName, onWidgetItem.getName());
                remoteViews.setTextViewText(R.id.itemCnt, onWidgetItem.getNow_count().toString() + "/" + String.valueOf(onWidgetItem.getCount() - onWidgetItem.getNow_count()));

                StringBuilder profit = new StringBuilder();
                Formatter fm = new Formatter(profit);
                fm.format("%,d", onWidgetItem.getProfit());

                remoteViews.setTextViewText(R.id.itemProfit, "+" + profit.toString());
                int image_resource_id = this.getResources().getIdentifier("item_" + String.valueOf(onWidgetItem.getId()), "drawable", "com.tarkus.merchantmonitor");
                if (0 == image_resource_id) {
                    image_resource_id = R.drawable.item_5819;
                }
                remoteViews.setImageViewResource(R.id.itemImage, image_resource_id);
            }
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }

    }

    private BroadcastReceiver bReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Recive intent: " + intent.getAction());
            if(intent.getAction().equals(UPDATE_MERC_LIST)) {
                sync_merc_list_with_db();

            }
            if(intent.getAction().equals(SHOW_TOAST)){
                Log.d(TAG, intent.getStringExtra("toast"));
                createInfoNotification(intent.getStringExtra("toast"));
                Toast.makeText(context, intent.getStringExtra("toast"), Toast.LENGTH_SHORT).show();
            }

        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        if (mNotificationManager!=null) {
            mNotificationManager.cancel(notificationID);
        }
    }

    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);

        ExpandableListView.ExpandableListContextMenuInfo info =
                (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;

        int type =
                ExpandableListView.getPackedPositionType(info.packedPosition);

        int group =
                ExpandableListView.getPackedPositionGroup(info.packedPosition);

        int child =
                ExpandableListView.getPackedPositionChild(info.packedPosition);


        // Only create a context menu for child items
        if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD)
        {
            getMenuInflater().inflate(R.menu.item_menu, menu);
        }
        // Only create a context menu for child items
        if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP)
        {
            getMenuInflater().inflate(R.menu.merc_menu, menu);
        }
    }

    public boolean onContextItemSelected(MenuItem menuItem)
    {
        ExpandableListView.ExpandableListContextMenuInfo info =
                (ExpandableListView.ExpandableListContextMenuInfo) menuItem.getMenuInfo();

        int groupPos = 0, childPos = 0, position = 0;

        int type = ExpandableListView.getPackedPositionType(info.packedPosition);
        if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD)
        {
            groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition);
            childPos = ExpandableListView.getPackedPositionChild(info.packedPosition);
        }
        if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP)
        {
            groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition);
        }


        switch (menuItem.getItemId())
        {
            case R.id.show:
                final Item item = (Item)  adapter.getChild(groupPos,childPos);//mercListView.getItemAtPosition(childPos);
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://market.lumiro.net/whosell.php?s="+item.getId().toString()+"&field=price&order=asc"));
                startActivity(browserIntent);
                return true;

            case R.id.toWidget:
                final Merc merc1 = (Merc) adapter.getGroup(groupPos);
                final Item item1 = (Item)  adapter.getChild(groupPos,childPos);
                setItemToWidget(merc1.getName(),item1.getId());
                updateWidget();
                // Перерисовываю список торговцев
                mercListView.setAdapter(adapter);
                mercListView.invalidate();
                return true;

            case R.id.del_merc:
                final Merc merc = (Merc) adapter.getGroup(groupPos);
                new AlertDialog.Builder(this)
                        .setTitle(merc.getName())
                        .setMessage(getString(R.string.confrim_delete_merc, merc.getName()))
                        .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                removeMerc(merc);
                            }
                        })
                        .setNegativeButton("Нет", null)
                        .show();

                return true;

            default:
                return super.onContextItemSelected(menuItem);
        }
    }


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        alarmSound = Uri.parse("android.resource://"+getPackageName()+"/"+ R.raw.ding);


        adapter = new MerchantArrayAdapter (this, new ArrayList());

        mercListView = (ExpandableListView)findViewById(R.id.listView);
        mercListView.setAdapter(adapter);
        mercListView.setClickable(true);
        registerForContextMenu(mercListView);
        sync_merc_list_with_db();




        LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UPDATE_MERC_LIST);
        intentFilter.addAction(SHOW_TOAST);
        bManager.registerReceiver(bReceiver, intentFilter);

        Intent i = new Intent(this, Market_Service.class);
        i.setAction(Market_Service.ACTION_SYNC_START);
        startService(i);
    }


    // Синхронизация списка торговцев с БД
    public void sync_merc_list_with_db(){
        DBAdaptor db = new DBAdaptor(this);

        try {
            List<Merc> mercs = db.getMercs();
            adapter.clear();
            for(Merc merc: mercs){
                adapter.add(merc);
            }
        }
        finally {
            db.close();
        }

        // Перерисовываю список торговцев
        mercListView.setAdapter(adapter);
        mercListView.invalidate();
        // Обновляю виджет
        updateWidget();
    }

    public void createInfoNotification(String message){

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("LumiRo")
                        .setContentText(message)
                        .setTicker("Обновление торговца")
                        .setNumber(++numMessages)
                        .setAutoCancel(true)
                        .setSound(alarmSound);

        Intent resultIntent = new Intent(this, MainActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(notificationID, mBuilder.build());
    }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
       // Inflate the menu items for use in the action bar
       MenuInflater inflater = getMenuInflater();
       inflater.inflate(R.menu.main_activity_actions, menu);
       return true;
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
       // Handle presses on the action bar items
       switch (item.getItemId()) {
           case R.id.action_sync:
               updateMerchants();
               return true;
           case R.id.action_add_merc:
               addMerc();
               return true;
           case R.id.action_options:
               Intent myIntent = new Intent(MainActivity.this, PreferencesActivity.class);
               MainActivity.this.startActivity(myIntent);
               return true;
           case R.id.action_about:

               Intent aboutIntent = new Intent(MainActivity.this, AboutActivity.class);
               MainActivity.this.startActivity(aboutIntent);
               return true;
           default:
               return super.onOptionsItemSelected(item);
       }
   }

    public void addMerc(){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(R.string.add_merc_dialog_title);
        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton("Добавить", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = input.getText().toString();
                Intent i = new Intent(getApplicationContext(), Market_Service.class);
                i.setAction(Market_Service.ACTION_SYNC_MERC);
                i.putExtra("merc_name", value);
                startService(i);
            }
        });
        alert.setNegativeButton("Отмена", null);

        alert.show();
    }

    public void removeMerc(Merc merc)
    {
        DBAdaptor db = new DBAdaptor(this);
        try {
            db.removeMerc(merc);
        }
        finally {
            db.close();
        }
        sync_merc_list_with_db();

        // Поскольку список торговцев хранится еще и в нашем сервисе, удаляем его и оттуда
        Intent i = new Intent(getApplicationContext(), Market_Service.class);
        i.setAction(Market_Service.ACTION_DEL_MERC);
        i.putExtra("merc_name", merc.getName());
        startService(i);

        Toast.makeText(this, merc.getName() + " удален", Toast.LENGTH_SHORT).show();

    }

    public void updateMerchants(){
        Intent i = new Intent(this, Market_Service.class);
        i.setAction(Market_Service.ACTION_SYNC_MERCS);
        startService(i);
    }

    public Item getItemToWidget()
    {
        sPref = getPreferences(MODE_PRIVATE);
        Item resItem = null;
        Integer itemID = sPref.getInt("item",0);

        DBAdaptor db = new DBAdaptor(this);
        try {
            Merc merc = db.getMercByName(sPref.getString("merc",""));
            if (merc == null)
            {
                return null;
            }

            List<Item> items = merc.getItems();
            for (Item item : items)
            {
                if (item.getId().equals(itemID))
                {
                    resItem = item;
                }
            }

        }
        finally {
            db.close();
        }
        return resItem;
    }



    public void setItemToWidget(String merc, Integer itemId)
    {
        sPref = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString("merc", merc);
        ed.putInt("item", itemId);
        ed.apply();
    }

}
