package com.tarkus.merchantmonitor;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.TextView;

/**
 * Created by Tarkus on 21.03.2015.
 */
public class LumiroWidget extends AppWidgetProvider {
    public static String ACTION_WIDGET_REQUEST = "ActionReceiverWidget.REQUEST";
    public static String ACTION_WIDGET_ANSWER = "ActionReceiverWidget.ANSWER";

    private String name = "";
    private Integer id = 0;
    private Integer cnt = 0;
    private Integer profit = 0;

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        //Создаем новый RemoteViews
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.main_widget);


        //обновляем виджет
        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
        Log.d("LumiRO", "UPDATE Widget! "+name);


        Intent i = new Intent(context, Market_Service.class);
        i.setAction(Market_Service.ACTION_SYNC_MERCS);
        context.startService(i);

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        //Ловим наш Broadcast, проверяем и выводим сообщение
        final String action = intent.getAction();
        if (ACTION_WIDGET_ANSWER.equals(action)) {
            try {
                name = intent.getStringExtra("name");
                id = Integer.parseInt(intent.getStringExtra("id"));
                cnt = Integer.parseInt(intent.getStringExtra("cnt"));
                profit = Integer.parseInt(intent.getStringExtra("profit"));
                Log.d("LumiRO", "Get in widget "+name);
            } catch (NullPointerException e) {
                Log.e("Error", "msg = null");
            }


        }
    }

}
