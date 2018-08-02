package com.example.halftough.webcomreader.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.example.halftough.webcomreader.ACLAdapter;
import com.example.halftough.webcomreader.R;
import com.example.halftough.webcomreader.RecyclerItemClickListener;
import com.example.halftough.webcomreader.webcoms.DilbertWebcom;
import com.example.halftough.webcomreader.webcoms.Webcom;
import com.example.halftough.webcomreader.webcoms.XkcdWebcom;

import java.util.ArrayList;
import java.util.List;

public class AddWebcomActivity extends AppCompatActivity {

    RecyclerView addWebcomList;
    List<Webcom> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_webcom);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        addWebcomList = (RecyclerView)findViewById(R.id.add_webcom_list);
        fillAvailableComicList();
    }

    private void fillAvailableComicList(){
        list = new ArrayList<Webcom>();

        list.add(new XkcdWebcom());
        list.add(new DilbertWebcom());
        //list.add(new Webcom(3,"Nemi"));
        //list.add(new Webcom(4,"Pepper & Carrot"));
        ACLAdapter adapter = new ACLAdapter(list);
        addWebcomList.setAdapter(adapter);
        addWebcomList.setLayoutManager(new LinearLayoutManager(this));

        addWebcomList.addOnItemTouchListener(new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                showInfoActivity(list.get(position));
            }
        }));

    }

    public void showInfoActivity(Webcom webcom){
        Intent addWebcomIntent = new Intent(this, WebcomInfoActivity.class);
        addWebcomIntent.putExtra(WebcomInfoActivity.WEBCOM_INFO_ID, webcom.getId());
        startActivityForResult(addWebcomIntent, 0);
    }

}
