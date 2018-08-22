package com.example.halftough.webcomreader.activities.ReadChapter;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.example.halftough.webcomreader.NoWebcomClassException;
import com.example.halftough.webcomreader.R;
import com.example.halftough.webcomreader.UserRepository;
import com.example.halftough.webcomreader.activities.ChapterList.ChapterListActivity;
import com.example.halftough.webcomreader.activities.MyWebcoms.MyWebcomsActivity;
import com.example.halftough.webcomreader.database.Chapter;
import com.example.halftough.webcomreader.database.ReadChapterRepository;
import com.example.halftough.webcomreader.webcoms.Webcom;

public class ReadChapterActivity extends AppCompatActivity {
    private Webcom webcom;
    private String number;
    ComicPageView readChapterImage;
    ReadChapterRepository readChapterRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.read_chapter_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        readChapterImage = (ComicPageView)findViewById(R.id.readChapterImage);

        Intent intent = getIntent();
        try {
            webcom = UserRepository.getWebcomInstance( intent.getStringExtra(MyWebcomsActivity.WEBCOM_ID) );
        } catch (NoWebcomClassException e) {
            //TODO exception
            e.printStackTrace();
        }

        number = intent.getStringExtra(ChapterListActivity.CHAPTER_NUMBER);

        readChapterRepository = new ReadChapterRepository(getApplication(), webcom, readChapterImage);
        setChapter(number);

    }

    @Override
    public void onBackPressed() {
        Intent result = new Intent();
        result.putExtra(ChapterListActivity.UPDATE_LIST, readChapterRepository.getUpdateMarker());
        setResult(RESULT_OK, result);
        super.onBackPressed();
    }

    public void setChapter(String c){
        readChapterRepository.setChapter(c);
        //readChapterRepository.getImageFor(readChapterImage);
    }

    public void nextPage() {
        readChapterRepository.nextChapter();
    }

    public void previousPage() {
        readChapterRepository.previousChapter();
    }
}
