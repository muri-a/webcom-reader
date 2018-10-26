package com.example.halftough.webcomreader.activities;

import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.halftough.webcomreader.R;
import com.example.halftough.webcomreader.UserRepository;
import com.example.halftough.webcomreader.webcoms.Webcom;

//TODO tags

public class WebcomInfoActivity extends AppCompatActivity {
    public final static String EXTRA_IS_READ = "EXTRA_IS_READ";
    private Webcom webcom;
    TextView title;
    TextView description;
    ImageView icon;
    TextView webpageTextView;
    TextView formatTextView;
    TextView pagesLabelTextView;
    TextView pagesTextView;
    Button addButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webcom_info_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        webcom = UserRepository.getWebcomInstance(intent.getStringExtra(UserRepository.EXTRA_WEBCOM_ID));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        title = (TextView)findViewById(R.id.webcomInfoTitle);
        description = (TextView)findViewById(R.id.webcomInfoDescription);
        icon = (ImageView)findViewById(R.id.webcomInfoIcon);
        webpageTextView = (TextView)findViewById(R.id.webcomInfoWebpage);
        formatTextView = (TextView)findViewById(R.id.webcomInfoFormat);
        pagesLabelTextView = (TextView)findViewById(R.id.webcomInfoPagesLabel);
        pagesTextView = (TextView)findViewById(R.id.webcomInfoPageNumber);
        addButton = (Button)findViewById(R.id.webcomInfoAddButton);

        title.setText(webcom.getTitle());
        setTitle(webcom.getTitle());
        webpageTextView.setText(webcom.getWebpage());
        description.setText(webcom.getDescription());
        icon.setImageDrawable(getResources().getDrawable(webcom.getIcon()));
        if( intent.getBooleanExtra(EXTRA_IS_READ, false) ){
            addButton.setEnabled(false);
        }
        switch (webcom.getFormat()){
            case PAGES:
                formatTextView.setText(getResources().getText(R.string.webcom_info_pages));
                pagesLabelTextView.setText(getResources().getText(R.string.webcom_info_pages_label));
                break;
            case CHAPTERS:
                formatTextView.setText(getResources().getText(R.string.webcom_info_chapters));
                pagesLabelTextView.setText(getResources().getText(R.string.webcom_info_chapters_label));
                break;
        }
        webcom.getChapterCount().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer integer) {
                pagesTextView.setText(integer.toString());
            }
        });
    }

    public void addWebcom(View view){
        Intent result = new Intent();
        result.putExtra(UserRepository.EXTRA_WEBCOM_ID, webcom.getId());
        setResult(AddWebcomActivity.ADD_WEBCOM_RESULT, result);
        finish();
    }

}
