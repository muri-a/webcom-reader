package com.example.halftough.webcomreader.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.TypeConverters;
import android.support.annotation.NonNull;

@Entity(tableName = "chapters", primaryKeys = {"wid", "chapter"})
public class Chapter implements Comparable<Chapter> {
    public enum Status{ UNREAD, READ, READING }
    @NonNull
    private String wid;
    @NonNull
    private String chapter;
    private String title;
    @NonNull
    @TypeConverters(StatusConverter.class)
    private Status status = Status.UNREAD;

    public Chapter(String wid, String chapter){
        this.wid = wid;
        this.chapter = chapter;
    }

    @NonNull
    public String getWid() { return wid; }
    public void setWid(String wid) { this.wid = wid; }

    @NonNull
    public String getChapter() { return chapter; }
    public void setChapter(String chapter) { this.chapter = chapter; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    @NonNull
    public Status getStatus() {
        return status;
    }
    public void setStatus(@NonNull Status status) {
        this.status = status;
    }

    @Override
    public int compareTo(@NonNull Chapter o) {
        return new Float(Float.parseFloat(chapter)).compareTo(Float.parseFloat(o.getChapter()));
    }

    @Override
    public boolean equals(Object o){
        if (o == null || !Chapter.class.isAssignableFrom(o.getClass())) {
            return false;
        }
        Chapter b = (Chapter)o;
        return wid == b.getWid() && chapter == b.getChapter();
    }
}
