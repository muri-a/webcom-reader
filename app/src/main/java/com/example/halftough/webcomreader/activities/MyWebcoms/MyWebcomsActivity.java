package com.example.halftough.webcomreader.activities.MyWebcoms;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.halftough.webcomreader.DownloaderService;
import com.example.halftough.webcomreader.GlobalPreferenceValue;
import com.example.halftough.webcomreader.R;
import com.example.halftough.webcomreader.UserRepository;
import com.example.halftough.webcomreader.activities.AddWebcomActivity;
import com.example.halftough.webcomreader.activities.ChapterList.ChapterListActivity;
import com.example.halftough.webcomreader.activities.ChapterList.ChapterPreferencesFragment;
import com.example.halftough.webcomreader.activities.GlobalSettingsActivity;
import com.example.halftough.webcomreader.database.ReadWebcom;

import java.util.ArrayList;
import java.util.List;

//TODO Removing webcoms
//TODO Autoupdates
public class MyWebcomsActivity extends AppCompatActivity {
    enum ActivityMode { NORMAL, SELECTING }
    public static int ADD_WEBCOM_RESULT = 1;

    RecyclerView libraryRecyclerView;
    MyWebcomsAdapter adapter;
    LibraryModel viewModel;
    SharedPreferences preferences;
    Toolbar selectingToolbar;
    ActivityMode mode = ActivityMode.NORMAL;

    List<ReadWebcom> selectedWebcoms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_webcoms_activity);

        PreferenceManager.setDefaultValues(this, UserRepository.GLOBAL_PREFERENCES, MODE_PRIVATE, R.xml.global_preferences, false);
        preferences = getSharedPreferences(UserRepository.GLOBAL_PREFERENCES, MODE_PRIVATE);

        libraryRecyclerView = (RecyclerView)findViewById(R.id.my_webcom_list);
        selectingToolbar = (Toolbar)findViewById(R.id.myWebcomsSelectingToolbar);
        selectedWebcoms = new ArrayList<>();

        getMenuInflater().inflate(R.menu.library_selecting_menu, selectingToolbar.getMenu());
        selectingToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.librerySelectingDelete:
                        new AlertDialog.Builder(MyWebcomsActivity.this)
                                .setMessage(R.string.library_delete_selected_dialog_message)
                                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        for(ReadWebcom webcom : selectedWebcoms){
                                            viewModel.deleteWebcom(webcom.getWid());
                                        }
                                        selectedWebcoms.clear();
                                        setModeNormal();
                                    }
                                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                }).create().show();
                        return true;
                }
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter = new MyWebcomsAdapter(this);
        libraryRecyclerView.setAdapter(adapter);

        viewModel = ViewModelProviders.of(this).get(LibraryModel.class);
        final Context context = this;
        viewModel.getAllReadWebcoms().observe(this, new Observer<List<ReadWebcom>>() {
            @Override
            public void onChanged(@Nullable List<ReadWebcom> readWebcoms) {
                Context con = context;
                adapter.setReadWebcoms(readWebcoms);
                //Set default preferences for all chapters
                for(ReadWebcom webcom : readWebcoms){
                    PreferenceManager.setDefaultValues(con, ChapterPreferencesFragment.PREFERENCE_KEY_COMIC+webcom.getWid(), MODE_PRIVATE, R.xml.chapter_preferences, false);
                }
            }
        });

        RecyclerView.LayoutManager layoutManager;
        if(preferences.getString("library_style", getString(R.string.global_preferences_librery_style_default)).equals("list")) {
            layoutManager = new LinearLayoutManager(this);
        }
        else{
            int spanCount = GlobalPreferenceValue.getCurrentGridCols(context, preferences);
            layoutManager = new GridLayoutManager(this, spanCount);
        }
        libraryRecyclerView.setLayoutManager(layoutManager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.library_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.myWebcomsMenuSettings:
                Intent intent = new Intent(this, GlobalSettingsActivity.class);
                startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    public ActivityMode getMode(){ return mode; }

    //Selects or unselects chapter, returns true if selected
    //TODO better animations
    public boolean triggerChapterSelect(ReadWebcom webcom){
        if(selectedWebcoms.contains(webcom)){
            selectedWebcoms.remove(webcom);
            selectingToolbar.setTitle(String.format(getString(R.string.library_selecting), selectedWebcoms.size()));
            if( selectedWebcoms.isEmpty()){
                setModeNormal();
            }
            return false;
        }
        else{
            selectedWebcoms.add(webcom);
            selectingToolbar.setTitle(String.format(getString(R.string.library_selecting), selectedWebcoms.size()));
            if(mode != ActivityMode.SELECTING) {
                mode = ActivityMode.SELECTING;
                getSupportActionBar().hide();
                selectingToolbar.setVisibility(View.VISIBLE);
            }
            return true;
        }
    }

    private void setModeNormal(){
        mode = ActivityMode.NORMAL;
        getSupportActionBar().show();
        selectingToolbar.setVisibility(View.GONE);
    }

    public void addNewComic(View view){
        Intent myIntent = new Intent(this, AddWebcomActivity.class);
        startActivityForResult(myIntent, ADD_WEBCOM_RESULT);
    }

    public void showChapterList(String wid){
        Intent intent = new Intent(this, ChapterListActivity.class);
        intent.putExtra(UserRepository.EXTRA_WEBCOM_ID, wid);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode== ADD_WEBCOM_RESULT && resultCode!=RESULT_CANCELED){
            if(data.hasExtra(UserRepository.EXTRA_WEBCOM_ID)){
                String wid = data.getStringExtra(UserRepository.EXTRA_WEBCOM_ID);
                viewModel.insert(new ReadWebcom(wid));
                DownloaderService.updateNewChaptersIn(this, wid);
            }
        }
    }
}
