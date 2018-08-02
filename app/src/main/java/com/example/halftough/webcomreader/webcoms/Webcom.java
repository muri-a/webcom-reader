package com.example.halftough.webcomreader.webcoms;

import android.graphics.drawable.Drawable;

public abstract class Webcom {

    public enum format { CHAPTERS, PAGES }

    protected String id;
    protected String title;
    protected String description;

    public String getId() { return id; }
    public String getTitle(){ return title; };
    public String getDescription(){ return description; };
    public abstract int getIcon();
    public abstract format getFormat();
    public abstract int getPageNumber(); //Returns number of all available pages/chapters of comic
}
