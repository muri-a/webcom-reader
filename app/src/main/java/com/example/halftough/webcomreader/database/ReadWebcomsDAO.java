package com.example.halftough.webcomreader.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import android.support.annotation.RequiresPermission;

import java.util.List;

@Dao
public interface ReadWebcomsDAO {
    @Query("SELECT * FROM read_webcoms")
    LiveData<List<ReadWebcom>> getAll();

    @Query("SELECT * FROM read_webcoms where wid=:wid")
    ReadWebcom get(String wid);

    @Query("SELECT chapterCount from read_webcoms WHERE wid=:wid")
    LiveData<Integer> getChapterCount(String wid);

    @Query("UPDATE read_webcoms SET lastRead = :date WHERE wid=:wid")
    void setLastReadDate(String wid, String date);

    @Query("UPDATE read_webcoms SET lastUpdated = :date WHERE wid=:wid")
    void setLastUpdateDate(String wid, String date);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ReadWebcom readWebcoms);

    @Query("DELETE FROM read_webcoms WHERE wid=:wid")
    void delete(String wid);

    @Query("UPDATE read_webcoms SET chapterCount = :count WHERE wid=:wid")
    void updateChapterCount(String wid, int count);

    @Query("UPDATE read_webcoms SET readChapters = :count WHERE wid=:wid")
    void updateReadChapterCount(String wid, int count);
}
