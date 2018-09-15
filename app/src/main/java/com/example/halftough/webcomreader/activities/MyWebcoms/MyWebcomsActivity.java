package com.example.halftough.webcomreader.activities.MyWebcoms;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.example.halftough.webcomreader.DownloaderService;
import com.example.halftough.webcomreader.R;
import com.example.halftough.webcomreader.RecyclerItemClickListener;
import com.example.halftough.webcomreader.UserRepository;
import com.example.halftough.webcomreader.activities.AddWebcomActivity;
import com.example.halftough.webcomreader.activities.ChapterList.ChapterListActivity;
import com.example.halftough.webcomreader.database.ReadWebcom;

import java.util.List;

//TODO Alternative views
public class MyWebcomsActivity extends AppCompatActivity {
    public static int ADD_WEBCOM_RESULT = 1;

    RecyclerView myWebcomRecyclerView;
    MyWebcomsViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_webcoms_activity);

        myWebcomRecyclerView = (RecyclerView)findViewById(R.id.my_webcom_list);
        final MyWebcomsAdapter adapter = new MyWebcomsAdapter(this);
        myWebcomRecyclerView.setAdapter(adapter);
        myWebcomRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        viewModel = ViewModelProviders.of(this).get(MyWebcomsViewModel.class);
        viewModel.getAllReadWebcoms().observe(this, new Observer<List<ReadWebcom>>() {
            @Override
            public void onChanged(@Nullable List<ReadWebcom> readWebcoms) {
                adapter.setReadWebcoms(readWebcoms);
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
