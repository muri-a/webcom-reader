package com.example.halftough.webcomreader.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;

import com.example.halftough.webcomreader.R;
import com.example.halftough.webcomreader.UserRepository;
import com.example.halftough.webcomreader.UserRepository.FieldType;

import java.util.Arrays;

import static android.content.Context.MODE_PRIVATE;

public class GlobalSettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    SharedPreferences sharedPreferences;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(UserRepository.GLOBAL_PREFERENCES);
        getPreferenceManager().setSharedPreferencesMode(MODE_PRIVATE);
        addPreferencesFromResource(R.xml.global_preferences);
    }

    @Override
    public void onResume() {
        super.onResume();

        sharedPreferences = getPreferenceManager().getSharedPreferences();
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        updateSummary("library_style");
        updateSummary("columns_vertical");
        updateSummary("columns_horizontal");
        updateSummary("chapter_order");
        updateSummary("autodownload");
        updateSummary("autodownload_number");
        updateSummary("autoremove_save");
    }

    @Override
    public void onPause() {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updateSummary(key);
    }

    void updateSummary(String key){
        int namesId = 0, valuesId = 0, defaultId = 0;
        FieldType type = null;
        switch (key){
            case "library_style":
                type = FieldType.ARRAY;
                namesId = R.array.global_preferences_library_style;
                valuesId = R.array.global_preferences_librery_style_values;
                defaultId = R.string.global_preferences_librery_style_default;
                break;
            case "columns_vertical":
                type = FieldType.SINGLE;
                defaultId = R.string.global_preferences_grid_columns_vertical_default;
                break;
            case "columns_horizontal":
                type = FieldType.SINGLE;
                defaultId = R.string.global_preferences_grid_columns_horizontal_default;
                break;
            case "chapter_order":
                type = FieldType.ARRAY;
                namesId = R.array.global_preferences_order_list;
                valuesId = R.array.global_perferences_order_list_values;
                defaultId = R.string.global_preferences_order_default;
                break;
            case "autodownload":
                type = FieldType.ARRAY;
                namesId = R.array.global_preferences_autodownload_list;
                valuesId = R.array.global_preferences_autodownload_values;
                defaultId = R.string.global_preferences_autodownload_default;
                break;
            case "autodownload_number":
                type = FieldType.SINGLE;
                defaultId = R.string.global_preferences_autodownload_number_default;
                break;
            case "autoremove_save":
                type = FieldType.SINGLE;
                defaultId = R.string.global_preferences_autoremoved_number_default;
                break;
        }
        if(type == null)
            return;
        String value = sharedPreferences.getString(key, getString(defaultId));
        if(type == FieldType.ARRAY) {
            String[] names = getResources().getStringArray(namesId);
            String[] values = getResources().getStringArray(valuesId);
            int index = Arrays.asList(values).indexOf(value);
            if (index >= 0)
                findPreference(key).setSummary(names[index]);
        }
        else{
            findPreference(key).setSummary(value);
        }
    }
}