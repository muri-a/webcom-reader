package com.example.halftough.webcomreader.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface ReadWebcomsDAO {
    @Query("SELECT * FROM read_webcoms")
    LiveData<List<ReadWebcom>> getAll();

    @Query("SELECT chapterCount from read_webcoms WHERE wid=:wid")
    LiveData<Integer> getChapterCount(String wid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ReadWebcom readWebcoms);
}
