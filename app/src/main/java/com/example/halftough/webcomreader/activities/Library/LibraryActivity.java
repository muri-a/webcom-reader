package com.example.halftough.webcomreader.activities.Library;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
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

import com.example.halftough.webcomreader.PreferenceHelper;
import com.example.halftough.webcomreader.R;
import com.example.halftough.webcomreader.UpdateWebcomsService;
import com.example.halftough.webcomreader.UserRepository;
import com.example.halftough.webcomreader.activities.AboutActivity;
import com.example.halftough.webcomreader.activities.AddWebcomActivity;
import com.example.halftough.webcomreader.activities.ChapterList.ChapterListActivity;
import com.example.halftough.webcomreader.activities.ChapterList.ChapterPreferencesFragment;
import com.example.halftough.webcomreader.activities.GlobalSettingsActivity;
import com.example.halftough.webcomreader.database.ReadWebcom;

import java.util.ArrayList;
import java.util.List;

public class LibraryActivity extends AppCompatActivity {
    enum ActivityMode { NORMAL, SELECTING }
    public static int ADD_WEBCOM_RESULT = 1;
    public static final String SORTING_KEY = "library_sorting";

    RecyclerView libraryRecyclerView;
    LibraryAdapter adapter;
    LibraryModel viewModel;
    SharedPreferences preferences;
    Toolbar selectingToolbar;
    ActivityMode mode = ActivityMode.NORMAL;

    List<ReadWebcom> selectedWebcoms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.library_activity);

        PreferenceManager.setDefaultValues(this, UserRepository.GLOBAL_PREFERENCES, MODE_PRIVATE, R.xml.global_preferences, false);
        preferences = getSharedPreferences(UserRepository.GLOBAL_PREFERENCES, MODE_PRIVATE);

        libraryRecyclerView = (RecyclerView)findViewById(R.id.my_webcom_list);
        selectingToolbar = (Toolbar)findViewById(R.id.myWebcomsSelectingToolbar);
        selectedWebcoms = new ArrayList<>();

        setTitle(R.string.title_activity_library);

        getMenuInflater().inflate(R.menu.library_selecting_menu, selectingToolbar.getMenu());
        selectingToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.librerySelectingDelete:
                        new AlertDialog.Builder(LibraryActivity.this)
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
        adapter = new LibraryAdapter(this);
        libraryRecyclerView.setAdapter(adapter);

        viewModel = ViewModelProviders.of(this).get(LibraryModel.class);
        viewModel.getAllReadWebcoms().observe(this, new Observer<List<ReadWebcom>>() {
            @Override
            public void onChanged(@Nullable List<ReadWebcom> readWebcoms) {
                viewModel.sort();
                adapter.setReadWebcoms(readWebcoms);
            }
        });

        RecyclerView.LayoutManager layoutManager;
        if(preferences.getString("library_style", getString(R.string.global_preferences_librery_style_default)).equals("list")) {
            layoutManager = new LinearLayoutManager(this);
        }
        else{
            int spanCount = PreferenceHelper.getCurrentGridCols(this, preferences);
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
            case R.id.libraryMenuSortingTitle:
                sortLibrary(LibraryModel.SortBy.TITLE);
                break;
            case R.id.libraryMenuSortingRead:
                sortLibrary(LibraryModel.SortBy.READ);
                break;
            case R.id.libraryMenuSortingUpdated:
                sortLibrary(LibraryModel.SortBy.UPDATED);
                break;
            case R.id.libraryMenuSortingUnread:
                sortLibrary(LibraryModel.SortBy.UNREAD);
                break;
            case R.id.libraryMenuSettings: {
                Intent intent = new Intent(this, GlobalSettingsActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.libraryMenuAbout: {
                Intent intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void sortLibrary(LibraryModel.SortBy choosenSort){
        LibraryModel.SortBy current = LibraryModel.SortBy.fromInt(preferences.getInt(SORTING_KEY, 0));
        if(current == choosenSort){
            preferences.edit().putInt(SORTING_KEY, choosenSort.reverse().toInt()).apply();
        }
        else{
            preferences.edit().putInt(SORTING_KEY, choosenSort.toInt()).apply();
        }
        viewModel.sort();
        adapter.notifyDataSetChanged();
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
                UpdateWebcomsService.updateNewChaptersIn(this, wid);
                //Set default preferences for chapters
                PreferenceManager.setDefaultValues(this, ChapterPreferencesFragment.PREFERENCE_KEY_COMIC+wid, MODE_PRIVATE, R.xml.chapter_preferences, false);
            }
        }
    }

    public List<ReadWebcom> getSelectedWebcoms(){ return selectedWebcoms; }
}
