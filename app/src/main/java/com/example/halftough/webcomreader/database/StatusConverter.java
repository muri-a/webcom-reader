package com.example.halftough.webcomreader.database;

import android.arch.persistence.room.TypeConverter;

public class StatusConverter {

    @TypeConverter
    public static Chapter.Status toStatus(int status){
        switch (status){
            case 0:
                return Chapter.Status.UNREAD;
            case 1:
                return Chapter.Status.READ;
            case 2:
                return Chapter.Status.READING;
            default:
                throw new IllegalArgumentException("Could not recognize status");
        }
    }

    @TypeConverter
    public static Integer toInteger(Chapter.Status status){
        switch (status){
            case UNREAD:
                return 0;
            case READ:
                return 1;
            case READING:
                return 2;
            default:
                throw new IllegalArgumentException("Could not recognize status");
        }
    }
}
