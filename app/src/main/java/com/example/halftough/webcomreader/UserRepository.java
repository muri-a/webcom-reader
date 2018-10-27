package com.example.halftough.webcomreader;

import android.content.Context;

import com.example.halftough.webcomreader.database.Chapter;
import com.example.halftough.webcomreader.webcoms.CyanideAndHappinessWebcom;
import com.example.halftough.webcomreader.webcoms.Webcom;
import com.example.halftough.webcomreader.webcoms.XkcdWebcom;

import java.io.File;


public class UserRepository {
        public enum FieldType{ ARRAY, STRING, TIME, SWITCH;}
    public static final String ACTION_CHAPTER_UPDATED = "com.example.halftough.webcomreader.ACTION_CHAPTER_UPDATED";

    public static final String GLOBAL_PREFERENCES = "com.example.halftough.webcomreader.GLOBAL_PREFERENCES";
    public static String EXTRA_WEBCOM_ID = "EXTRA_WEBCOM_ID";

    public static String EXTRA_CHAPTER_NUMBER = "EXTRA_CHAPTER_NUMBER";

    private static int notificationID = 1000;

    public static Webcom getWebcomInstance(String id) {
        switch(id){
            case "cyanideandhappiness":
                return new CyanideAndHappinessWebcom();
            case "xkcd":
                return new XkcdWebcom();
            default:
                return null;
        }
    }

    //TODO temorary location. might change in future
    public static void deleteChapter(Chapter chapter){
        File file = chapter.getFile();
        if(file != null) {
            file.delete();
        }
    }

    public static int nextNotificationID() {
        return notificationID++;
    }

    public static String parseHumanTimeFromMinutes(Context context, int minutes) {
        int days = 0 , hours = 0;
        String string = "";

        days = minutes/(24*60);
        minutes -= days*24*60;

        hours = minutes/60;
        minutes -= hours*60;

        if(days > 0){
            string += String.format("%s "+context.getString(R.string.days), days);
        }
        if(hours > 0){
            if(!string.isEmpty()){
                string += " ";
            }
            string += String.format("%s "+context.getString(R.string.hours), hours);
        }
        if(minutes > 0){
            if(!string.isEmpty()){
                string += " ";
            }
            string += String.format("%s "+context.getString(R.string.minutes), minutes);
        }
        if(string.isEmpty()){
            string = string += String.format("%s "+context.getString(R.string.minutes), minutes);
        }
        return string;
    }
}
