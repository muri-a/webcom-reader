package com.example.halftough.webcomreader;

import com.example.halftough.webcomreader.database.Chapter;
import com.example.halftough.webcomreader.webcoms.CyanideAndHappinessWebcom;
import com.example.halftough.webcomreader.webcoms.Webcom;
import com.example.halftough.webcomreader.webcoms.XkcdWebcom;

import java.io.File;


public class UserRepository {
    public static final String ACTION_CHAPTER_UPDATED = "com.example.halftough.webcomreader.ACTION_CHAPTER_UPDATED";
    public static final String GLOBAL_PREFERENCES = "com.example.halftough.webcomreader.GLOBAL_PREFERENCES";

    public static String EXTRA_WEBCOM_ID = "EXTRA_WEBCOM_ID";
    public static String EXTRA_CHAPTER_NUMBER = "EXTRA_CHAPTER_NUMBER";

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
        file.delete();
    }
}
