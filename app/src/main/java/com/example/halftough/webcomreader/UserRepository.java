package com.example.halftough.webcomreader;

import com.example.halftough.webcomreader.webcoms.DilbertWebcom;
import com.example.halftough.webcomreader.webcoms.Webcom;
import com.example.halftough.webcomreader.webcoms.XkcdWebcom;


public class UserRepository {
    public static final String ACTION_CHAPTER_UPDATED = "com.example.halftough.webcomreader.ACTION_CHAPTER_UPDATED";
    public static final String GLOBAL_PREFERENCES = "com.example.halftough.webcomreader.GLOBAL_PREFERENCES";

    public static String EXTRA_WEBCOM_ID = "EXTRA_WEBCOM_ID";
    public static String EXTRA_CHAPTER_NUMBER = "EXTRA_CHAPTER_NUMBER";

    static public Webcom getWebcomInstance(String id) {
        switch(id){
            case "dilbert":
                return new DilbertWebcom();
            case "xkcd":
                return new XkcdWebcom();
            default:
                return null;
        }
    }

}
