package com.example.halftough.webcomreader.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.TypeConverters;
import android.arch.persistence.room.Update;

import java.util.List;
import java.util.Set;

@Dao
public interface ChaptersDAO {
    @Query("SELECT * FROM chapters WHERE wid=:wid ORDER BY CAST(chapter AS REAL)")
    LiveData<List<Chapter>> getChapters(String wid);

    @Query("SELECT * FROM chapters WHERE wid=:wid ORDER BY CAST(chapter AS REAL) DESC")
    LiveData<List<Chapter>> getChaptersDesc(String wid);



    @Query("SELECT * FROM chapters WHERE wid=:wid AND CAST(chapter AS REAL) > CAST(:chapter AS REAL) ORDER BY CAST(chapter AS REAL) LIMIT 1")
    LiveData<Chapter> getNext(String wid, String chapter);

    @Query("SELECT * FROM chapters WHERE wid=:wid AND CAST(chapter AS REAL) < CAST(:chapter AS REAL) ORDER BY CAST(chapter AS REAL) DESC LIMIT 1")
    LiveData<Chapter> getPrevious(String wid, String chapter);

    @Query("SELECT * FROM chapters ORDER BY CAST(chapter AS REAL) LIMIT 1")
    LiveData<Chapter> getFirstChapter();

    @Query("SELECT * FROM chapters ORDER BY CAST(chapter AS REAL) DESC LIMIT 1")
    LiveData<Chapter> getLastChapter();

    //Fixes status in case of error. Only use at start of the application.
    @Query("UPDATE chapters SET DownloadStatus=0 WHERE DownloadStatus=1")
    void clearUndownloaded();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Chapter chapter);

    @Update
    void update(Chapter chapter);

    @Update
    void update(List<Chapter> chapters);

    @Query("DELETE FROM chapters WHERE wid=:wid")
    void deleteWebcom(String wid);

    @Query("UPDATE chapters SET Status=:status WHERE wid=:wid AND chapter=:chapter")
    @TypeConverters(StatusConverter.class)
    void setStatus(String wid, String chapter, Chapter.Status status);

    @Query("UPDATE chapters SET DownloadStatus=:status WHERE wid=:wid AND chapter=:chapter")
    @TypeConverters(DownloadStatusConverter.class)
    void setDownloadStatus(String wid, String chapter, Chapter.DownloadStatus status);

    @Query("SELECT * FROM chapters WHERE wid LIKE :wid AND chapter LIKE :number")
    LiveData<Chapter> getChapter(String wid, String number);

}
