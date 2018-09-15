package com.example.halftough.webcomreader.activities.ReadChapter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.halftough.webcomreader.UserRepository;
import com.example.halftough.webcomreader.database.Chapter;

public class ReadChapterBroadcastReceiver extends BroadcastReceiver {
    ReadChapterActivity activity;
    Chapter currentChapter;
    public ReadChapterBroadcastReceiver(ReadChapterActivity activity, Chapter currentChapter){
        this.activity = activity;
        this.currentChapter = currentChapter;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String rWid = intent.getStringExtra(UserRepository.EXTRA_WEBCOM_ID);
        String rNumber = intent.getStringExtra(UserRepository.EXTRA_CHAPTER_NUMBER);
        if(currentChapter.getChapter().equals(rNumber) && currentChapter.getWid().equals(rWid)){
            activity.imageDownloaded();
        }
    }
}
