package com.example.halftough.webcomreader.activities;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.example.halftough.webcomreader.R;
import com.example.halftough.webcomreader.RecyclerItemClickListener;
import com.example.halftough.webcomreader.UserRepository;
import com.example.halftough.webcomreader.database.AppDatabase;
import com.example.halftough.webcomreader.database.ReadWebcom;
import com.example.halftough.webcomreader.database.ReadWebcomsDAO;
import com.example.halftough.webcomreader.webcoms.CyanideAndHappinessWebcom;
import com.example.halftough.webcomreader.webcoms.Webcom;
import com.example.halftough.webcomreader.webcoms.XkcdWebcom;

import java.util.ArrayList;
import java.util.List;

public class AddWebcomActivity extends AppCompatActivity {
    public static int ADD_WEBCOM_RESULT = 1;

    RecyclerView addWebcomRecyclerView;
    List<Webcom> list;
    private ReadWebcomsDAO readWebcomsDAO;
    LiveData<List<ReadWebcom>> read;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_webcom_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        addWebcomRecyclerView = (RecyclerView)findViewById(R.id.add_webcom_list);

        AppDatabase db = AppDatabase.getDatabase(getApplication());
        readWebcomsDAO = db.readWebcomsDAO();
        read = readWebcomsDAO.getAll();
        read.observe(this, new Observer<List<ReadWebcom>>() {
            @Override
            public void onChanged(@Nullable List<ReadWebcom> readWebcoms) {
                fillAvailableComicList();
            }
        });
    }

    private void fillAvailableComicList(){
        list = new ArrayList<Webcom>();

        list.add(new CyanideAndHappinessWebcom());
        list.add(new XkcdWebcom());
        //list.add(new Webcom(3,"Pepper & Carrot"));
        AddWebcomAdapter adapter = new AddWebcomAdapter(this, list);
        addWebcomRecyclerView.setAdapter(adapter);
        addWebcomRecyclerView.setLayoutManager(new LinearLayoutManager(this));

    }

    public void showInfoActivity(Webcom webcom){
        Intent addWebcomIntent = new Intent(this, WebcomInfoActivity.class);
        addWebcomIntent.putExtra(UserRepository.EXTRA_WEBCOM_ID, webcom.getId());
        addWebcomIntent.putExtra(WebcomInfoActivity.EXTRA_IS_READ, isWebcomRead(webcom.getId()));
        startActivityForResult(addWebcomIntent, ADD_WEBCOM_RESULT);
    }

    public boolean isWebcomRead(String wid){
        if(read.getValue() == null){
            return false;
        }
        for(ReadWebcom rw : read.getValue()){
            if( rw.getWid().equals(wid) ){
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==ADD_WEBCOM_RESULT && resultCode!=RESULT_CANCELED){
            if(data.hasExtra(UserRepository.EXTRA_WEBCOM_ID)) {
                String wid = data.getStringExtra(UserRepository.EXTRA_WEBCOM_ID);
                Intent result = new Intent();
                result.putExtra(UserRepository.EXTRA_WEBCOM_ID, wid);
                setResult(RESULT_OK, result);
                finish();
            }
        }
    }
}
