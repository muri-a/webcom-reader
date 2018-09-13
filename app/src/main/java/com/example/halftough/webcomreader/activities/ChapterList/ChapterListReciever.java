package com.example.halftough.webcomreader.activities.ChapterList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.halftough.webcomreader.activities.MyWebcoms.MyWebcomsActivity;

public class ChapterListReciever extends BroadcastReceiver {
    public static final String ACTION_CHAPTER_UPDATED = "com.example.halftough.webcomreader.ACTION_CHAPTER_UPDATED";
    private ChapterListViewModel chapterListModel;
    private String wid;

    public ChapterListReciever(ChapterListViewModel model, String wid){
        super();
        chapterListModel = model;
        this.wid = wid;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        String recWid = intent.getStringExtra(MyWebcomsActivity.WEBCOM_ID);
        if(wid.equals(recWid))
            chapterListModel.update();
    }
}
