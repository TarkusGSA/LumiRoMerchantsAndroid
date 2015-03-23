package com.tarkus.merchantmonitor;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


public class AboutActivity extends Activity {
    private static final String TAG = "net.lumiro.about";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        LumiOnlineRequest request = new LumiOnlineRequest();
        request.delegate = this;

        request.execute("http://www.lumiro.net/online.ajax");


/*        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String online = extras.getString("online_cnt");
            TextView onlineText = (TextView) findViewById(R.id.onlineText);
            onlineText.setText(online);
        }*/

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_about, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
/*        if (id == R.id.action_settings) {
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }
}
