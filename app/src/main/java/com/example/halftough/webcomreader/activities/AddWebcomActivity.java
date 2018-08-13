package com.example.halftough.webcomreader.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.example.halftough.webcomreader.R;
import com.example.halftough.webcomreader.RecyclerItemClickListener;
import com.example.halftough.webcomreader.activities.MyWebcoms.MyWebcomsActivity;
import com.example.halftough.webcomreader.webcoms.DilbertWebcom;
import com.example.halftough.webcomreader.webcoms.Webcom;
import com.example.halftough.webcomreader.webcoms.XkcdWebcom;

import java.util.ArrayList;
import java.util.List;

public class AddWebcomActivity extends AppCompatActivity {
    public static int ADD_WEBCOM_RESULT = 1;

    RecyclerView addWebcomRecyclerView;
    List<Webcom> list;

    //TODO hide added

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_webcom_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        addWebcomRecyclerView = (RecyclerView)findViewById(R.id.add_webcom_list);
        fillAvailableComicList();
    }

    private void fillAvailableComicList(){
        list = new ArrayList<Webcom>();

        list.add(new XkcdWebcom());
        list.add(new DilbertWebcom());
        //list.add(new Webcom(3,"Nemi"));
        //list.add(new Webcom(4,"Pepper & Carrot"));
        AddWebcomAdapter adapter = new AddWebcomAdapter(list);
        addWebcomRecyclerView.setAdapter(adapter);
        addWebcomRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        addWebcomRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                showInfoActivity(list.get(position));
            }
        }));

    }

    public void showInfoActivity(Webcom webcom){
        Intent addWebcomIntent = new Intent(this, WebcomInfoActivity.class);
        addWebcomIntent.putExtra(WebcomInfoActivity.WEBCOM_INFO_ID, webcom.getId());
        startActivityForResult(addWebcomIntent, ADD_WEBCOM_RESULT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==ADD_WEBCOM_RESULT && resultCode!=RESULT_CANCELED){
            if(data.hasExtra(MyWebcomsActivity.WEBCOM_ID)) {
                String wid = data.getStringExtra(MyWebcomsActivity.WEBCOM_ID);
                Intent result = new Intent();
                result.putExtra(MyWebcomsActivity.WEBCOM_ID, wid);
                setResult(MyWebcomsActivity.ADD_WEBCOM_RESULT, result);
                finish();
            }
        }
    }
}
