package com.example.halftough.webcomreader.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
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

        readChapterRepository = new ReadChapterRepository(this, webcom);
        readChapterImage = (ImageView)findViewById(R.id.readChapterImage);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, webcom.getTitle()+": "+number, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        readChapterRepository.getImageFor(number, readChapterImage);

    }

}
