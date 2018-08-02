package com.example.halftough.webcomreader.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.halftough.webcomreader.NoWebcomClassException;
import com.example.halftough.webcomreader.R;
import com.example.halftough.webcomreader.UserRepository;
import com.example.halftough.webcomreader.webcoms.Webcom;

public class WebcomInfoActivity extends AppCompatActivity {
    public static String WEBCOM_INFO_ID;
    private Webcom webcom;
    TextView title;
    TextView description;
    ImageView icon;
    TextView formatTextView;
    TextView pagesLabelTextView;
    TextView pagesTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webcom_info);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        try {
            webcom = UserRepository.getWebcomInstance(intent.getStringExtra(WEBCOM_INFO_ID));
        } catch (NoWebcomClassException e) {
            e.printStackTrace();
            //TODO better error handling
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, webcom.getTitle(), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        title = (TextView)findViewById(R.id.webcomInfoTitle);
        description = (TextView)findViewById(R.id.webcomInfoDescription);
        icon = (ImageView)findViewById(R.id.webcomInfoIcon);
        formatTextView = (TextView)findViewById(R.id.webcomInfoFormat);
        pagesLabelTextView = (TextView)findViewById(R.id.webcomInfoPagesLabel);
        pagesTextView = (TextView)findViewById(R.id.webcomInfoPageNumber);

        title.setText(webcom.getTitle());
        description.setText(webcom.getDescription());
        icon.setImageDrawable(getResources().getDrawable(webcom.getIcon()));
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
        pagesTextView.setText(Integer.toString(webcom.getPageNumber()));
    }

}
