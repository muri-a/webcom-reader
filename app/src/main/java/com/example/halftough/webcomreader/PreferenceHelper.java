package com.example.halftough.webcomreader;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import com.example.halftough.webcomreader.activities.ChapterList.ChapterPreferencesFragment;
import com.example.halftough.webcomreader.webcoms.Webcom;

public class PreferenceHelper {
    public enum AutodownloadSetting { NONE, NEWEST, OLDEST;}

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

    static AutodownloadSetting getAutodownloadSetting(Context context, SharedPreferences chapterPreferences, SharedPreferences globalPreferences, Webcom webcom){
        if(chapterPreferences.getBoolean("autodownload_global", true)){
            String autodownload = globalPreferences.getString("autodownload", context.getString(R.string.global_preferences_autodownload_default));
            switch(autodownload){
                case "none":
                    return AutodownloadSetting.NONE;
                case "default":
                    if(webcom.getReadingOrder() == Webcom.ReadingOrder.NEWEST_FIRST)
                        return AutodownloadSetting.NEWEST;
                    else
                        return AutodownloadSetting.OLDEST;
                case "oldest":
                    return AutodownloadSetting.OLDEST;
                case "newest":
                    return AutodownloadSetting.NEWEST;
            }
        }
        else{
            String autodownload = chapterPreferences.getString("autodownload", context.getString(R.string.chapter_preferences_autodownload_default));
            switch(autodownload){
                case "none":
                    return AutodownloadSetting.NONE;
                case "oldest":
                    return AutodownloadSetting.OLDEST;
                case "newest":
                    return AutodownloadSetting.NEWEST;
            }
        }
        return AutodownloadSetting.NONE;
    }

    public static int getAutodownloadnumber(Context context, SharedPreferences chapterPreferences, SharedPreferences globalPreferences) {
        String autodownloadnumber;
        if(chapterPreferences.getBoolean("autodownload_global", true)){
            autodownloadnumber = globalPreferences.getString("autodownload_number", context.getString(R.string.global_preferences_autodownload_number_default));
        }
        else{
            autodownloadnumber = chapterPreferences.getString("autodownload_number", context.getString(R.string.global_preferences_autodownload_number_default));
        }
        return Integer.parseInt(autodownloadnumber);
    }

    public static int getAutoremoveSave(Context context, String wid) {
        SharedPreferences chapterPreferences = context.getSharedPreferences(ChapterPreferencesFragment.PREFERENCE_KEY_COMIC+wid, Context.MODE_PRIVATE);
        SharedPreferences globalPreferences = context.getSharedPreferences(UserRepository.GLOBAL_PREFERENCES, Context.MODE_PRIVATE);
        String autoremoveSave;
        if(chapterPreferences.getBoolean("autoremove_global", true)){
            autoremoveSave = globalPreferences.getString("autoremove_save", context.getString(R.string.global_preferences_autoremove_save_default));
        }
        else{
            autoremoveSave = chapterPreferences.getString("autoremove_save", context.getString(R.string.global_preferences_autodownload_number_default));
        }
        return Integer.parseInt(autoremoveSave);
    }

    public static boolean getAutoremove(Context context, String wid){
        SharedPreferences chapterPreferences = context.getSharedPreferences(ChapterPreferencesFragment.PREFERENCE_KEY_COMIC+wid, Context.MODE_PRIVATE);
        SharedPreferences globalPreferences = context.getSharedPreferences(UserRepository.GLOBAL_PREFERENCES, Context.MODE_PRIVATE);
        if(chapterPreferences.getBoolean("autoremove_global", true)){
            return globalPreferences.getBoolean("autoremove", true);
        }
        else{
            return chapterPreferences.getBoolean("autoremove", true);
        }
    }
}
