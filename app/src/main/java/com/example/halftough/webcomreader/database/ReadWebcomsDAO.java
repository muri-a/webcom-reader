package com.example.halftough.webcomreader.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.example.halftough.webcomreader.webcoms.Webcom;

import java.util.List;

@Dao
public interface ReadWebcomsDAO {
    @Query("SELECT * FROM readwebcoms")
    LiveData<List<ReadWebcoms>> getAll();

    @Insert
    void insert(ReadWebcoms readWebcoms);
}
