package com.example.gateswitch;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class UserSettingsActivity extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings);

    }
}