package com.tarkus.merchantmonitor;

import android.os.Bundle;
import android.preference.PreferenceActivity;


/**
 * Created by Tarkus on 13.03.2015.
 */

public class PreferencesActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}