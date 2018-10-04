package com.example.halftough.webcomreader.activities.ReadChapter;


import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.example.halftough.webcomreader.R;
import com.example.halftough.webcomreader.UserRepository;
import com.example.halftough.webcomreader.activities.ChapterList.ChapterListActivity;
import com.example.halftough.webcomreader.database.Chapter;
import com.example.halftough.webcomreader.database.ReadChapterRepository;
import com.example.halftough.webcomreader.webcoms.Webcom;

public class ReadChapterActivity extends AppCompatActivity {
    private Webcom webcom;
    ComicPageView readChapterImage;
    ReadChapterRepository readChapterRepository;
    ReadChapterBroadcastReceiver broadcastReceiver;
    TextView downloadingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.read_chapter_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        webcom = UserRepository.getWebcomInstance( intent.getStringExtra(UserRepository.EXTRA_WEBCOM_ID) );

        downloadingView = (TextView)findViewById(R.id.readChapterDownloadingTextView);
        readChapterImage = (ComicPageView)findViewById(R.id.readChapterImage);
        readChapterRepository = new ReadChapterRepository(this, webcom, readChapterImage);
        setChapter( intent.getStringExtra(UserRepository.EXTRA_CHAPTER_NUMBER) );
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstance) {
        String number = readChapterRepository.getChapterNumber();
        savedInstance.putString(UserRepository.EXTRA_CHAPTER_NUMBER, number);
        super.onSaveInstanceState(savedInstance);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        String number = savedInstanceState.getString(UserRepository.EXTRA_CHAPTER_NUMBER);
        setChapter(number);
    }

    @Override
    protected void onResume() {
        super.onResume();
        readChapterImage.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        readChapterImage.stop();
        if(broadcastReceiver!=null) {
            unregisterReceiver(broadcastReceiver);
            broadcastReceiver = null;
        }
    }

    @Override
    public void onBackPressed() {
        readChapterImage.stop();
        Intent result = new Intent();
        result.putExtra(ChapterListActivity.UPDATE_LIST, readChapterRepository.getUpdateMarker());
        setResult(RESULT_OK, result);
        super.onBackPressed();
    }

    public void setChapter(String c){
        readChapterRepository.setChapter(c);
    }

    public void nextPage() {
        readChapterRepository.nextChapter();
    }

    public void previousPage() {
        readChapterRepository.previousChapter();
    }

    public Webcom getWebcom(){ return webcom; }

    public void hideDownloadingText(){
        downloadingView.setVisibility(View.GONE);
    }

    public void showDownloadingText(){
        downloadingView.setText(R.string.read_chapter_downloading);
        downloadingView.setVisibility(View.VISIBLE);
    }

    public void showCouldntDownloadText() {
        downloadingView.setText(R.string.read_chapter_cant_download);
        downloadingView.setVisibility(View.VISIBLE);
    }

    public void listenForDownload(Chapter chapter){
        broadcastReceiver = new ReadChapterBroadcastReceiver(this, chapter);
        IntentFilter filter = new IntentFilter(UserRepository.ACTION_CHAPTER_UPDATED);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(broadcastReceiver, filter);
    }

    public void imageDownloaded() {
        if(broadcastReceiver!=null) {
            unregisterReceiver(broadcastReceiver);
            broadcastReceiver = null;
        }
        readChapterRepository.getImageFromStorage();
    }
}
