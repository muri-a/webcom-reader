package com.example.halftough.webcomreader.activities.ChapterList;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.example.halftough.webcomreader.R;
import com.example.halftough.webcomreader.RecyclerItemClickListener;
import com.example.halftough.webcomreader.activities.MyWebcoms.MyWebcomsActivity;
import com.example.halftough.webcomreader.activities.ReadChapterActivity;
import com.example.halftough.webcomreader.database.Chapter;

import java.util.List;

public class ChapterListActivity extends AppCompatActivity {
    public static String CHAPTER_NUMBER = "CHAPTER_NUMBER";
    RecyclerView chapterListRecyclerView;
    ChapterListViewModel viewModel;
    String wid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chapter_list_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        wid = intent.getStringExtra(MyWebcomsActivity.WEBCOM_ID);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, new Integer(viewModel.getChapters().getValue().size()).toString(), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        chapterListRecyclerView = (RecyclerView)findViewById(R.id.chapterListRecyclerView);
        final ChapterListAdapter adapter = new ChapterListAdapter(this);
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

        chapterListRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Chapter chapter = viewModel.getChapters().getValue().get(position);
                readWebcom(chapter);
            }
        }));

    }

    void readWebcom(Chapter chapter){
        Intent intent = new Intent(this, ReadChapterActivity.class);
        intent.putExtra(MyWebcomsActivity.WEBCOM_ID, chapter.getWid());
        intent.putExtra(CHAPTER_NUMBER, chapter.getChapter());
        startActivity(intent);
    }

}
