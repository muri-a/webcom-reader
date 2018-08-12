package com.example.halftough.webcomreader.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;
import java.util.Set;

@Dao
public interface ChaptersDAO {
    @Query("SELECT * FROM chapters WHERE wid=:wid ORDER BY CAST(chapter AS REAL)")
    LiveData<List<Chapter>> getChapters(String wid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Chapter chapter);

}
