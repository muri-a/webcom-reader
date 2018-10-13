package com.example.halftough.webcomreader.activities.ChapterList;

import android.Manifest;
import android.app.DialogFragment;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.halftough.webcomreader.ChapterFilter;
import com.example.halftough.webcomreader.DownloaderService;
import com.example.halftough.webcomreader.R;
import com.example.halftough.webcomreader.UserRepository;
import com.example.halftough.webcomreader.activities.ReadChapter.ReadChapterActivity;
import com.example.halftough.webcomreader.database.Chapter;

import java.util.List;

//TODO kep position of the list on screen rotate
public class ChapterListActivity extends AppCompatActivity implements PickNumberDialog.NoticeNumberPickerListener {
    public static int READ_CHAPTER_RESULT = 3;
    public static int SETTINGS_RESULT = 4;
    public static String UPDATE_LIST = "UPDATE_LIST";
    RecyclerView chapterListRecyclerView;
    ChapterListAdapter adapter;
    ChapterListViewModel viewModel;
    ChapterListReciever reciever;
    SharedPreferences chapterPreferences;
    Menu menu;
    String wid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chapter_list_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        wid = intent.getStringExtra(UserRepository.EXTRA_WEBCOM_ID);

        String title = UserRepository.getWebcomInstance(wid).getTitle();
        setTitle(title);
        chapterPreferences = getSharedPreferences(ChapterPreferencesFragment.PREFERENCE_KEY_COMIC+wid, MODE_PRIVATE);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //TODO hide button when list is empty
        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO depending on settings, newest or oldest unread
                Chapter chapter = viewModel.getChapterToRead();
                readWebcom(chapter);
            }
        });

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
                ChapterFilter filter = makeChapterFilter();
                adapter.changeFilter(filter);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chapter_list_toolbar_menu, menu);
        this.menu = menu;
        updateFilterMenu();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.chapterListFiltersMenuUnread:
            case R.id.chapterListFiltersMenuRead:
            case R.id.chapterListFiltersMenuDownloaded:
            case R.id.chapterListFiltersMenuUndownloaded:
                changeFilter(item);
                break;
            case R.id.chapterListFiltersMenuClear:
                chapterPreferences.edit().putBoolean("filter_read", false).putBoolean("filter_unread", false)
                        .putBoolean("filter_downloaded", false).putBoolean("filter_undownloaded", false).apply();
                updateFilterMenu();
                adapter.clearFilter();
                break;
            case R.id.chapterListToolbarMenuChangeOrder:
                changeOrder();
                break;
            case R.id.chapterListToolbarDownload1: {
                viewModel.downloadNextChapters(1);
                break;
            }
            case R.id.chapterListToolbarDownload10: {
                //TODO would be even nicer if they would download in odrer
                viewModel.downloadNextChapters(10);
                break;
            }
            case R.id.chapterListToolbarDownloadCustom:
                DialogFragment dialog = new PickNumberDialog();
                dialog.show(getFragmentManager(), "PickNumberDialog");
                break;
            case R.id.chapterListToolbarMenuSettings:
                Intent intent = new Intent(this, ChapterPreferencesActivity.class);
                intent.putExtra(UserRepository.EXTRA_WEBCOM_ID, wid);
                startActivityForResult(intent, SETTINGS_RESULT);
        }
        return super.onOptionsItemSelected(item);
    }

    private void changeFilter(MenuItem item) {
        String setting_key = null;
        switch (item.getItemId()) {
            case R.id.chapterListFiltersMenuRead:
                setting_key = "filter_read";
                menu.findItem(R.id.chapterListFiltersMenuUnread).setEnabled(item.isChecked());
                break;
            case R.id.chapterListFiltersMenuUnread:
                setting_key = "filter_unread";
                menu.findItem(R.id.chapterListFiltersMenuRead).setEnabled(item.isChecked());
                break;
            case R.id.chapterListFiltersMenuDownloaded:
                setting_key = "filter_downloaded";
                menu.findItem(R.id.chapterListFiltersMenuUndownloaded).setEnabled(item.isChecked());
                break;
            case R.id.chapterListFiltersMenuUndownloaded:
                setting_key = "filter_undownloaded";
                menu.findItem(R.id.chapterListFiltersMenuDownloaded).setEnabled(item.isChecked());
                break;
        }
        item.setChecked(!item.isChecked());
        chapterPreferences.edit().putBoolean(setting_key, item.isChecked()).apply();
        ChapterFilter filter = makeChapterFilter();
        adapter.changeFilter(filter);
    }

    private void updateFilterMenu(){
        boolean read = chapterPreferences.getBoolean("filter_read", false);
        boolean unread = chapterPreferences.getBoolean("filter_unread", false);
        boolean downloaded = chapterPreferences.getBoolean("filter_downloaded", false);
        boolean undownloaded = chapterPreferences.getBoolean("filter_undownloaded", false);
        menu.findItem(R.id.chapterListFiltersMenuRead).setChecked(read);
        menu.findItem(R.id.chapterListFiltersMenuRead).setEnabled(!unread);
        menu.findItem(R.id.chapterListFiltersMenuUnread).setChecked(unread);
        menu.findItem(R.id.chapterListFiltersMenuUnread).setEnabled(!read);
        menu.findItem(R.id.chapterListFiltersMenuDownloaded).setChecked(downloaded);
        menu.findItem(R.id.chapterListFiltersMenuDownloaded).setEnabled(!undownloaded);
        menu.findItem(R.id.chapterListFiltersMenuUndownloaded).setChecked(undownloaded);
        menu.findItem(R.id.chapterListFiltersMenuUndownloaded).setEnabled(!downloaded);
    }

    private ChapterFilter makeChapterFilter() {
        ChapterFilter filter = new ChapterFilter();
        filter.setRead(chapterPreferences.getBoolean("filter_read", false));
        filter.setUnread(chapterPreferences.getBoolean("filter_unread", false));
        filter.setDownloaded(chapterPreferences.getBoolean("filter_downloaded", false));
        filter.setUndownloaded(chapterPreferences.getBoolean("filter_undownloaded", false));
        return filter;
    }

    @Override
    public void onNumberPickerPositiveClick(int value) {
        viewModel.downloadNextChapters(value);
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
        else if(requestCode == SETTINGS_RESULT){
            viewModel.update();
            DownloaderService.autodownload(this, wid);
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        unregisterReceiver(reciever);
    }

    public void changeOrder(){
        viewModel.changeOrder();
    }

    public void readWebcom(Chapter chapter){
        if(chapter==null)
            return;
        viewModel.markWebcomBeingRead();
        Intent intent = new Intent(this, ReadChapterActivity.class);
        intent.putExtra(UserRepository.EXTRA_WEBCOM_ID, chapter.getWid());
        intent.putExtra(UserRepository.EXTRA_CHAPTER_NUMBER, chapter.getChapter());
        startActivityForResult(intent, READ_CHAPTER_RESULT);
    }

    public ChapterListViewModel getViewModel() {
        return viewModel;
    }

    public void downloadChapter(Chapter chapter) {
        if( ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        viewModel.downloadChapter(chapter);
    }

    public void deleteChapter(Chapter chapter) {
        viewModel.deleteChapter(chapter);
    }
}
