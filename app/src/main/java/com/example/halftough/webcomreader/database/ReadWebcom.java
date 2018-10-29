package com.example.halftough.webcomreader.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;


@Entity(tableName = "read_webcoms")
public class ReadWebcom {
    @PrimaryKey
    @NonNull
    private String wid;
    private int chapterCount = 0;
    private int readChapters = 0;
    private String lastRead;
    private String lastUpdated;
    @Nullable
    private String extra = null;

    public ReadWebcom(String wid){
        this.wid = wid;
    }

    public String getWid() {
        return wid;
    }
    public void setWid(String wid) {
        this.wid = wid;
    }
    public int getChapterCount() {
        return chapterCount;
    }
    public void setChapterCount(int chapterCount) {
        this.chapterCount = chapterCount;
    }
    public int getReadChapters() {
        return readChapters;
    }
    public void setReadChapters(int readChapters) {
        this.readChapters = readChapters;
    }
    public String getLastRead() {
        return lastRead;
    }
    public void setLastRead(String lastRead) {
        this.lastRead = lastRead;
    }
    public String getLastUpdated() {
        return lastUpdated;
    }
    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    public String getExtra() {
        return extra;
    }
    public void setExtra(String extra) {
        this.extra = extra;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ReadWebcom){
            return wid.equals( ((ReadWebcom)obj).getWid() );
        }
        else{
            return super.equals(obj);
        }
    }
}
