package com.example.halftough.webcomreader.activities.MyWebcoms;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.halftough.webcomreader.DownloaderService;
import com.example.halftough.webcomreader.R;
import com.example.halftough.webcomreader.RecyclerItemClickListener;
import com.example.halftough.webcomreader.UserRepository;
import com.example.halftough.webcomreader.activities.AddWebcomActivity;
import com.example.halftough.webcomreader.activities.ChapterList.ChapterListActivity;
import com.example.halftough.webcomreader.activities.ChapterList.ChapterPreferencesFragment;
import com.example.halftough.webcomreader.activities.GlobalSettingsActivity;
import com.example.halftough.webcomreader.database.ReadWebcom;

import java.util.List;

//TODO Alternative views
//TODO Removing webcoms
//TODO Autoupdates
public class MyWebcomsActivity extends AppCompatActivity {
    public static int ADD_WEBCOM_RESULT = 1;

    RecyclerView myWebcomRecyclerView;
    MyWebcomsViewModel viewModel;
    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_webcoms_activity);

        PreferenceManager.setDefaultValues(this, UserRepository.GLOBAL_PREFERENCES, MODE_PRIVATE, R.xml.global_preferences, false);
        preferences = getSharedPreferences(UserRepository.GLOBAL_PREFERENCES, MODE_PRIVATE);

        myWebcomRecyclerView = (RecyclerView)findViewById(R.id.my_webcom_list);
        final MyWebcomsAdapter adapter = new MyWebcomsAdapter(this);
        myWebcomRecyclerView.setAdapter(adapter);


        viewModel = ViewModelProviders.of(this).get(MyWebcomsViewModel.class);
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

        myWebcomRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                String wid = viewModel.getAllReadWebcoms().getValue().get(position).getWid();
                showChapterList(wid);
            }
        }));
    }

    @Override
    protected void onResume() {
        super.onResume();
        RecyclerView.LayoutManager layoutManager;
        if(preferences.getString("library_style", getString(R.string.global_preferences_librery_style_default)).equals("list")) {
            layoutManager = new LinearLayoutManager(this);
        }
        else{
            layoutManager = new GridLayoutManager(this, 3);
        }
        myWebcomRecyclerView.setLayoutManager(layoutManager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_webcoms_menu, menu);
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
