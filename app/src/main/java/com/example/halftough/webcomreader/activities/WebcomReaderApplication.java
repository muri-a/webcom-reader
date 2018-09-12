package com.example.halftough.webcomreader.activities;

import android.app.Application;

import com.example.halftough.webcomreader.DownloaderService;

public class WebcomReaderApplication extends Application {

    @Override
    public void onCreate(){
        super.onCreate();
        DownloaderService.updateNewChapters(this);
    }
}
