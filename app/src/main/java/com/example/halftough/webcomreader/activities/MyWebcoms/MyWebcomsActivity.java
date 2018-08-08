package com.example.halftough.webcomreader.activities.MyWebcoms;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.example.halftough.webcomreader.AddWebcomAdapter;
import com.example.halftough.webcomreader.NoWebcomClassException;
import com.example.halftough.webcomreader.R;
import com.example.halftough.webcomreader.UserRepository;
import com.example.halftough.webcomreader.activities.AddWebcomActivity;
import com.example.halftough.webcomreader.database.AppDatabase;
import com.example.halftough.webcomreader.database.ReadWebcoms;
import com.example.halftough.webcomreader.webcoms.Webcom;

import java.util.ArrayList;
import java.util.List;

//TODO Alternative views

public class MyWebcomsActivity extends AppCompatActivity {
    public static String ADD_WEBCOM_ID = "ADD_WEBCOM_ID";
    public static int ADD_WEBCOM_RESULT = 1;

    List<Webcom> myWebcoms;
    RecyclerView myWebcomList;
    MyWebcomsViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_webcoms_activity);

        myWebcomList = (RecyclerView)findViewById(R.id.my_webcom_list);
        final MyWebcomsAdapter adapter = new MyWebcomsAdapter(this);
        myWebcomList.setAdapter(adapter);
        myWebcomList.setLayoutManager(new GridLayoutManager(this, 2));

        viewModel = ViewModelProviders.of(this).get(MyWebcomsViewModel.class);
        viewModel.getAllReadWebcoms().observe(this, new Observer<List<ReadWebcoms>>() {
            @Override
            public void onChanged(@Nullable List<ReadWebcoms> readWebcoms) {
                adapter.setReadWebcoms(readWebcoms);
            }
        });
    }

    public void addNewComic(View view){
        Intent myIntent = new Intent(this, AddWebcomActivity.class);
        startActivityForResult(myIntent, ADD_WEBCOM_RESULT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode== ADD_WEBCOM_RESULT && resultCode!=RESULT_CANCELED){
            if(data.hasExtra(ADD_WEBCOM_ID)){
                String wid = data.getStringExtra(ADD_WEBCOM_ID);
                viewModel.insert(new ReadWebcoms(wid));
            }
        }
    }
}
