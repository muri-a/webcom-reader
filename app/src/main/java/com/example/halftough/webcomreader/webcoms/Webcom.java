package com.example.halftough.webcomreader.webcoms;

import android.arch.lifecycle.LiveData;
import android.graphics.drawable.Drawable;

import com.example.halftough.webcomreader.database.Chapter;

import java.util.List;

public abstract class Webcom {

    public enum format { CHAPTERS, PAGES }

    protected String id;
    protected String title;
    protected String description;

    public String getId() { return id; }
    public String getTitle(){ return title; }
    public String getDescription(){ return description; }
    public abstract int getIcon();
    public abstract format getFormat();
    public abstract int getPageCount(); //Returns number of all available pages/chapters of comic
    public abstract String[] getTags();
    public abstract LiveData<List<Chapter>> getChapters();
    public abstract String[] getLanguages();
}
