package com.example.halftough.webcomreader.database;

import android.arch.persistence.room.TypeConverter;

class DownloadStatusConverter {
    @TypeConverter
    public static Chapter.DownloadStatus toDownloadStatus(int downloadStatus){
        switch(downloadStatus){
            case 0:
            default:
                return Chapter.DownloadStatus.UNDOWNLOADED;
            case 1:
                return Chapter.DownloadStatus.DOWNLOADING;
            case 2:
                return Chapter.DownloadStatus.DOWNLOADED;
        }
    }

    @TypeConverter
    public static Integer toInteger(Chapter.DownloadStatus downloadStatus){
        switch(downloadStatus){
            case UNDOWNLOADED:
                return 0;
            case DOWNLOADING:
                return 1;
            case DOWNLOADED:
                return 2;
            default:
                throw new IllegalArgumentException("Could not recognize status");
        }
    }
}
