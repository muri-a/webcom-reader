package com.example.halftough.webcomreader.database;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;
import android.support.annotation.NonNull;

@Database(entities = {ReadWebcom.class, Chapter.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {

    public abstract ReadWebcomsDAO readWebcomsDAO();
    public abstract ChaptersDAO chaptersDAO();

    private static AppDatabase INSTANCE;

    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE chapters ADD COLUMN extra TEXT DEFAULT NULL");
            database.execSQL("ALTER TABLE read_webcoms ADD COLUMN extra TEXT DEFAULT NULL");

        }
    };

    public static AppDatabase getDatabase(final Context context){
        if (INSTANCE == null){
            synchronized (AppDatabase.class){
                if(INSTANCE == null && context != null){
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "database")
                            .addMigrations(MIGRATION_1_2)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

}
