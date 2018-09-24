package com.example.halftough.webcomreader.activities;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.example.halftough.webcomreader.R;

public class GlobalSettingsActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GlobalSettingsFragment fragment = new GlobalSettingsFragment();
        getFragmentManager().beginTransaction().replace(android.R.id.content, fragment).commit();
    }
}
