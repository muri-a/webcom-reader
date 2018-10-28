package com.example.halftough.webcomreader.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.example.halftough.webcomreader.R;
import com.example.halftough.webcomreader.SheduledUpdateReceiver;
import com.example.halftough.webcomreader.UpdateWebcomsService;
import com.example.halftough.webcomreader.UserRepository;
import com.example.halftough.webcomreader.UserRepository.FieldType;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static android.content.Context.ALARM_SERVICE;
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
        switch (key){
            case "autoupdate":
                if( sharedPreferences.getBoolean("autoupdate", true) ){
                    int minutes = sharedPreferences.getInt("autoupdate_time", 120);

                    Intent intent = new Intent(getActivity(), SheduledUpdateReceiver.class);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity().getApplicationContext(), UpdateWebcomsService.UPDATE_BROADCAST_REQUEST_CODE, intent, 0);
                    AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(ALARM_SERVICE);
                    alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + minutes*60000, pendingIntent);
                }
                else{
                    Intent intent = new Intent(getActivity(), SheduledUpdateReceiver.class);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity().getApplicationContext(), UpdateWebcomsService.UPDATE_BROADCAST_REQUEST_CODE, intent, 0);
                    AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(ALARM_SERVICE);
                    alarmManager.cancel(pendingIntent);
                }
                break;
            case "autoupdate_time":
                int minutes = sharedPreferences.getInt("autoupdate_time", 120);

                Intent intent = new Intent(getActivity(), SheduledUpdateReceiver.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity().getApplicationContext(), UpdateWebcomsService.UPDATE_BROADCAST_REQUEST_CODE, intent, 0);
                AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(ALARM_SERVICE);
                alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + minutes*60000, pendingIntent);
                break;
        }
    }

    void updateSummary(String key){
        int namesId = 0, valuesId = 0;
        String defaultVal = "";
        int defaultItnVal = 0;
        FieldType type = null;
        List<Preference> preferences = new LinkedList<>();
        switch (key){
            case "library_style":
                type = FieldType.ARRAY;
                namesId = R.array.global_preferences_library_style;
                valuesId = R.array.global_preferences_librery_style_values;
                defaultVal = getString(R.string.global_preferences_librery_style_default);
                break;
            case "columns_vertical":
                type = FieldType.STRING;
                defaultVal = getString(R.string.global_preferences_grid_columns_vertical_default);
                break;
            case "columns_horizontal":
                type = FieldType.STRING;
                defaultVal = getString(R.string.global_preferences_grid_columns_horizontal_default);
                break;
            case "chapter_order":
                type = FieldType.ARRAY;
                namesId = R.array.global_preferences_order_list;
                valuesId = R.array.global_perferences_order_list_values;
                defaultVal = getString(R.string.global_preferences_order_default);
                break;
            case "autodownload":
                type = FieldType.ARRAY;
                namesId = R.array.global_preferences_autodownload_list;
                valuesId = R.array.global_preferences_autodownload_values;
                defaultVal = getString(R.string.global_preferences_autodownload_default);
                break;
            case "autodownload_number":
                type = FieldType.STRING;
                defaultVal = getString(R.string.global_preferences_autodownload_number_default);
                break;
            case "autoremove_save":
                type = FieldType.STRING;
                defaultVal = getString(R.string.global_preferences_autoremove_save_default);
                break;
            case "autoupdate":
                type = FieldType.SWITCH;
                preferences.add(findPreference("autoupdate_time"));
                break;
            case "autoupdate_time":
                type = FieldType.TIME;
                defaultItnVal = 120;
                break;
        }
        if(type == null)
            return;
        if(type == FieldType.ARRAY) {
            String value = sharedPreferences.getString(key, defaultVal);
            String[] names = getResources().getStringArray(namesId);
            String[] values = getResources().getStringArray(valuesId);
            int index = Arrays.asList(values).indexOf(value);
            if (index >= 0)
                findPreference(key).setSummary(names[index]);
        }
        else if(type == FieldType.TIME){
            int minutes = sharedPreferences.getInt(key, defaultItnVal);
            findPreference(key).setSummary( UserRepository.parseHumanTimeFromMinutes(getActivity(), minutes) );
        }
        else if(type == FieldType.SWITCH){
            boolean state = sharedPreferences.getBoolean(key, true);
            for(Preference preference : preferences){
                preference.setEnabled(state);
            }
        }
        else{
            String value = sharedPreferences.getString(key, defaultVal);
            findPreference(key).setSummary(value);
        }
    }
}