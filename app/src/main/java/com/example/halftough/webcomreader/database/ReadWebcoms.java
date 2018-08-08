package com.example.halftough.webcomreader.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;


@Entity
public class ReadWebcoms {
    @PrimaryKey
    @NonNull
    private String wid;

    public ReadWebcoms(String wid){
        this.wid = wid;
    }

    public String getWid() {
        return wid;
    }

    public void setWid(String wid) {
        this.wid = wid;
    }
}
