package com.example.halftough.webcomreader;

import android.content.SharedPreferences;
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
}
