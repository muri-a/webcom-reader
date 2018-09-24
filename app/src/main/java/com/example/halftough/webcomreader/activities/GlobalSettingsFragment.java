package com.example.halftough.webcomreader.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.example.halftough.webcomreader.R;
import com.example.halftough.webcomreader.UserRepository;

import static android.content.Context.MODE_PRIVATE;

public class GlobalSettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(UserRepository.GLOBAL_PREFERENCES);
        getPreferenceManager().setSharedPreferencesMode(MODE_PRIVATE);
        addPreferencesFromResource(R.xml.global_preferences);
    }
}