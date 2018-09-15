package com.example.halftough.webcomreader.activities.ChapterList;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.halftough.webcomreader.DownloaderService;
import com.example.halftough.webcomreader.NoWebcomClassException;
import com.example.halftough.webcomreader.R;
import com.example.halftough.webcomreader.UserRepository;
import com.example.halftough.webcomreader.activities.ReadChapter.ReadChapterActivity;
import com.example.halftough.webcomreader.database.Chapter;

import java.util.List;

public class ChapterListActivity extends AppCompatActivity {
    public static int READ_CHAPTER_RESULT = 3;
    public static String UPDATE_LIST = "UPDATE_LIST";
    RecyclerView chapterListRecyclerView;
    ChapterListAdapter adapter;
    ChapterListViewModel viewModel;
    ChapterListReciever reciever;
    String wid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chapter_list_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        wid = intent.getStringExtra(UserRepository.EXTRA_WEBCOM_ID);

        try {
            String title = UserRepository.getWebcomInstance(wid).getTitle();
            setTitle(title);
        } catch (NoWebcomClassException e) {
            e.printStackTrace();
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        chapterListRecyclerView = (RecyclerView)findViewById(R.id.chapterListRecyclerView);
        adapter = new ChapterListAdapter(this);
        chapterListRecyclerView.setAdapter(adapter);
        chapterListRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        viewModel = ViewModelProviders.of(this).get(ChapterListViewModel.class);
        viewModel.setWid(wid);
        viewModel.getChapters().observe(this, new Observer<List<Chapter>>() {
            @Override
            public void onChanged(@Nullable List<Chapter> chapters) {
                adapter.setChapters(chapters);
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO depending on settings, newest or oldest unread
                Chapter chapter = viewModel.getChapterToRead();
                readWebcom(chapter);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chapter_list_toolbar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.chapterListToolbarMenuFilter:
            case R.id.chapterListToolbarMenuChangeOrder:
            case R.id.chapterListToolbarDownload1: {
                List<Chapter> chapters = viewModel.getChaptersToDownload(1);
                for (Chapter chapter : chapters) {
                    downloadChapter(chapter);
                }
                break;
            }
            case R.id.chapterListToolbarDownload10: {
                //TODO would be even nicer if they would download in odrer
                List<Chapter> chapters = viewModel.getChaptersToDownload(10);
                for (Chapter chapter : chapters) {
                    downloadChapter(chapter);
                }
                break;
            }
            case R.id.chapterListToolbarDownloadCustom:
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        reciever = new ChapterListReciever(viewModel, wid);
        IntentFilter filter = new IntentFilter(UserRepository.ACTION_CHAPTER_UPDATED);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(reciever, filter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == READ_CHAPTER_RESULT && resultCode!=RESULT_CANCELED){
            boolean update = data.getBooleanExtra(UPDATE_LIST, false);
            if(update){
                viewModel.update();
            }
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        unregisterReceiver(reciever);
    }

    public void readWebcom(Chapter chapter){
        Intent intent = new Intent(this, ReadChapterActivity.class);
        intent.putExtra(UserRepository.EXTRA_WEBCOM_ID, chapter.getWid());
        intent.putExtra(UserRepository.EXTRA_CHAPTER_NUMBER, chapter.getChapter());
        startActivityForResult(intent, READ_CHAPTER_RESULT);
    }

    public ChapterListViewModel getViewModel() {
        return viewModel;
    }

    public void downloadChapter(Chapter chapter) {
        viewModel.downloadChapter(chapter);
    }
}
