package com.example.halftough.webcomreader.activities.ChapterList;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;

import com.example.halftough.webcomreader.R;
import com.example.halftough.webcomreader.UserRepository;

public class ChapterPreferencesFragment extends PreferenceFragment {
    public static final String PREFERENCE_KEY_COMIC = "com.example.halftough.webcomreader.COMIC_PREFERENCE_";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        getPreferenceManager().setSharedPreferencesName(PREFERENCE_KEY_COMIC+bundle.getString(UserRepository.EXTRA_WEBCOM_ID));
        addPreferencesFromResource(R.xml.chapter_preferences);

    }
}
