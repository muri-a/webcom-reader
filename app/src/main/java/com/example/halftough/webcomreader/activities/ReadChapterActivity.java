package com.example.halftough.webcomreader.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;

import com.example.halftough.webcomreader.NoWebcomClassException;
import com.example.halftough.webcomreader.R;
import com.example.halftough.webcomreader.UserRepository;
import com.example.halftough.webcomreader.activities.ChapterList.ChapterListActivity;
import com.example.halftough.webcomreader.activities.MyWebcoms.MyWebcomsActivity;
import com.example.halftough.webcomreader.database.ReadChapterRepository;
import com.example.halftough.webcomreader.webcoms.Webcom;

public class ReadChapterActivity extends AppCompatActivity {
    private Webcom webcom;
    private String number;
    ImageView readChapterImage;
    ReadChapterRepository readChapterRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.read_chapter_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        try {
            webcom = UserRepository.getWebcomInstance( intent.getStringExtra(MyWebcomsActivity.WEBCOM_ID) );
            number = intent.getStringExtra(ChapterListActivity.CHAPTER_NUMBER);
        } catch (NoWebcomClassException e) {
            //TODO exception
            e.printStackTrace();
        }

        readChapterRepository = new ReadChapterRepository(getApplication(), webcom, number);
        readChapterImage = (ImageView)findViewById(R.id.readChapterImage);

        readChapterRepository.getImageFor(number, readChapterImage);

    }

    @Override
    public void onBackPressed() {
        Intent result = new Intent();
        result.putExtra(ChapterListActivity.UPDATE_LIST, readChapterRepository.getUpdateMarker());
        setResult(RESULT_OK, result);
        super.onBackPressed();
    }
}
