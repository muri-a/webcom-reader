package com.example.halftough.webcomreader.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.TypeConverters;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.FilenameFilter;

@Entity(tableName = "chapters", primaryKeys = {"wid", "chapter"})
public class Chapter implements Comparable<Chapter> {
    public enum Status { UNREAD, READ, READING }
    public enum DownloadStatus { UNDOWNLOADED, DOWNLOADING, DOWNLOADED }

    @NonNull
    private String wid;
    @NonNull
    private String chapter;
    private String title;
    @NonNull
    @TypeConverters(StatusConverter.class)
    private Status status = Status.UNREAD;

    @NonNull
    @TypeConverters(DownloadStatusConverter.class)
    private DownloadStatus downloadStatus = DownloadStatus.UNDOWNLOADED;

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

    @NonNull
    public DownloadStatus getDownloadStatus() {
        return downloadStatus;
    }

    public void setDownloadStatus(@NonNull DownloadStatus downloadStatus) {
        this.downloadStatus = downloadStatus;
    }

    @Override
    public int compareTo(@NonNull Chapter o) {
        return Float.valueOf(Float.parseFloat(chapter)).compareTo(Float.parseFloat(o.getChapter()));
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !Chapter.class.isAssignableFrom(o.getClass())) {
            return false;
        }
        Chapter b = (Chapter)o;
        return wid == b.getWid() && chapter == b.getChapter();
    }

    public File getFile() {
        //TODO option to load from internal or external
        File root = android.os.Environment.getExternalStorageDirectory();
        File dir = new File(root.getAbsolutePath()+"/webcom/"+wid);
        File [] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith(getChapter()+".") || name.equals(getChapter());
            }
        });
        //TODO if more than one file, pick "best"
        return files!=null && files.length>0?files[0]:null;
    }
}
