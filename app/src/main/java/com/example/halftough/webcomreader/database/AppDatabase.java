package com.example.halftough.webcomreader.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

@Database(entities = {ReadWebcom.class, Chapter.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    public abstract ReadWebcomsDAO readWebcomsDAO();
    public abstract ChaptersDAO chaptersDAO();

    private static AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context){
        if (INSTANCE == null){
            synchronized (AppDatabase.class){
                if(INSTANCE == null && context != null){
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "database").build();
                }
            }
        }
        return INSTANCE;
    }
}
