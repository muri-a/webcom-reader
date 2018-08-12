package com.example.halftough.webcomreader.database;

import android.arch.persistence.room.Entity;
import android.support.annotation.NonNull;

@Entity(tableName = "chapters", primaryKeys = {"wid", "chapter"})
public class Chapter {
    @NonNull
    private String wid;
    @NonNull
    private String chapter;
    private String title;

    public Chapter(String wid, String chapter){
        this.wid = wid;
        this.chapter = chapter;
    }

    public String getWid() { return wid; }
    public void setWid(String wid) { this.wid = wid; }

    public String getChapter() { return chapter; }
    public void setChapter(String chapter) { this.chapter = chapter; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
}
