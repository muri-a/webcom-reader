package com.example.halftough.webcomreader.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface ReadWebcomsDAO {
    @Query("SELECT * FROM read_webcoms")
    LiveData<List<ReadWebcom>> getAll();

    @Query("SELECT chapterCount from read_webcoms WHERE wid=:wid")
    LiveData<Integer> getChapterCount(String wid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ReadWebcom readWebcoms);

    @Query("UPDATE read_webcoms SET chapterCount = :count WHERE wid=:wid")
    void updateChapterCount(String wid, int count);

    @Query("UPDATE read_webcoms SET readChapters = :count WHERE wid=:wid")
    void updateReadChapterCount(String wid, int count);
}
