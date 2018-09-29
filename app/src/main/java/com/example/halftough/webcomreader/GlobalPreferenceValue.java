package com.example.halftough.webcomreader;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.Preference;
import android.preference.PreferenceManager;

import com.example.halftough.webcomreader.webcoms.Webcom;

public class GlobalPreferenceValue {
    public static String getChapterOrder(SharedPreferences global, Webcom webcom, String pref) {
        String s;
        if (pref.equals("global")) {
            s = global.getString("chapter_order", "");
            if(s.equals("smart")){
                if(webcom.getReadingOrder() == Webcom.ReadingOrder.NEWEST_FIRST){
                    return "decreasing";
                }
                else{
                    return "ascending";
                }
            }
            else{
                return s;
            }
        }
        return pref;
    }

    public static int getCurrentGridCols(Context context, SharedPreferences preferences){
        if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            String defaultVal = context.getString(R.string.global_preferences_grid_columns_vertical_default);
            return Integer.parseInt(preferences.getString("columns_horizontal", defaultVal));
        }
        else{
            String defaultVal = context.getString(R.string.global_preferences_grid_columns_horizontal_default);
            return Integer.parseInt(preferences.getString("columns_vertical", defaultVal));
        }
    }
}
