package com.example.halftough.webcomreader.activities.ChapterList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.example.halftough.webcomreader.UserRepository;

public class ChapterPreferencesActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String wid = intent.getStringExtra(UserRepository.EXTRA_WEBCOM_ID);

        ChapterPreferencesFragment fragment = new ChapterPreferencesFragment();
        Bundle bundle = new Bundle();
        bundle.putString(UserRepository.EXTRA_WEBCOM_ID, wid);
        fragment.setArguments(bundle);
        getFragmentManager().beginTransaction().replace(android.R.id.content, fragment).commit();
    }
}
