package com.example.halftough.webcomreader.activities.ChapterList;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;

import com.example.halftough.webcomreader.R;
import com.example.halftough.webcomreader.UserRepository;
import com.example.halftough.webcomreader.UserRepository.FieldType;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class ChapterPreferencesFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    SharedPreferences sharedPreferences, globalPreferences;
    public static final String PREFERENCE_KEY_COMIC = "com.halftough.webcomreader.COMIC_PREFERENCE_";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        getPreferenceManager().setSharedPreferencesName(PREFERENCE_KEY_COMIC+bundle.getString(UserRepository.EXTRA_WEBCOM_ID));
        getPreferenceManager().setSharedPreferencesMode(MODE_PRIVATE);
        addPreferencesFromResource(R.xml.chapter_preferences);
    }

    @Override
    public void onResume() {
        super.onResume();

        sharedPreferences = getPreferenceManager().getSharedPreferences();
        globalPreferences = getActivity().getSharedPreferences(UserRepository.GLOBAL_PREFERENCES, MODE_PRIVATE);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        for(String key : sharedPreferences.getAll().keySet()){
            updateSummary(key);
        }
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
        int namesId = 0, valuesId = 0;
        List<Preference> preferences = new LinkedList<>();
        String defaultVal = "";
        FieldType type = null;
        switch (key){
            case "chapter_order":
                type = FieldType.ARRAY;
                namesId = R.array.chapter_perferences_order_list;
                valuesId = R.array.chapter_perferences_order_list_values;
                defaultVal = getString(R.string.chapter_preferences_order_default);
                break;
            case "autodownload_global":
                type = FieldType.SWITCH;
                preferences.add(findPreference("autodownload"));
                preferences.add(findPreference("autodownload_number"));
                break;
            case "autodownload":
                type = FieldType.ARRAY;
                namesId = R.array.chapter_preferences_autodownload_list;
                valuesId = R.array.chapter_preferences_autodownload_values;
                defaultVal = getString(R.string.chapter_preferences_autodownload_default);
                break;
            case "autodownload_number":
                type = FieldType.STRING;
                defaultVal = globalPreferences.getString("autodownload_number", getString(R.string.global_preferences_autodownload_number_default));
                break;
            case "autoremove_global":
                type = FieldType.SWITCH;
                preferences.add(findPreference("autoremove"));
                preferences.add(findPreference("autoremove_save"));
                break;
            case "autoremove_save":
                type = FieldType.STRING;
                defaultVal = globalPreferences.getString("autoremove_save", getString(R.string.global_preferences_autoremove_save_default));
                break;
        }
        if(type == null)
            return;
        switch (type){
            case ARRAY: {
                String value = sharedPreferences.getString(key, defaultVal);
                String[] names = getResources().getStringArray(namesId);
                String[] values = getResources().getStringArray(valuesId);
                int index = Arrays.asList(values).indexOf(value);
                if (index >= 0)
                    findPreference(key).setSummary(names[index]);
            }
            break;
            case STRING: {
                String value = sharedPreferences.getString(key, defaultVal);
                findPreference(key).setSummary(value);
            }
            break;
            case SWITCH:{
                boolean state = sharedPreferences.getBoolean(key, true);
                for(Preference preference : preferences){
                    preference.setEnabled(!state);
                }
            }
            break;
        }
    }
}
