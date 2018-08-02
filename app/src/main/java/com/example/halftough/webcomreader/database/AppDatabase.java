package com.example.halftough.webcomreader.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(entities = {ReadWebcoms.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    //public abstract WebcomDao webcomDao();
}
