package com.example.halftough.webcomreader.database;

import android.arch.persistence.room.Entity;
import android.support.annotation.NonNull;

@Entity(tableName = "chapters", primaryKeys = {"wid", "chapter"})
public class Chapter {
    @NonNull
    private String wid;
    private int chapter;
    private String title;

    public Chapter(String wid, int chapter){
        this.wid = wid;
        this.chapter = chapter;
    }

    public String getWid() { return wid; }
    public void setWid(String wid) { this.wid = wid; }

    public int getChapter() { return chapter; }
    public void setChapter(int chapter) { this.chapter = chapter; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
}
