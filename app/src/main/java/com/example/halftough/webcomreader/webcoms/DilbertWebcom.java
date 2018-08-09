package com.example.halftough.webcomreader.webcoms;

import android.arch.lifecycle.LiveData;

import com.example.halftough.webcomreader.R;
import com.example.halftough.webcomreader.database.Chapter;

import java.util.List;

public class DilbertWebcom extends Webcom {
    public DilbertWebcom(){
        id = "dilbert";
        title = "Dilbert";
    }

    @Override
    public int getIcon() {
        return R.mipmap.dilbert_ico;
    }

    @Override
    public format getFormat() {
        return format.PAGES;
    }

    @Override
    public int getPageCount() {
        return 1;
    }

    @Override
    public String[] getTags() {
        String[] tags =  {"funny"};
        return tags;
    }

    @Override
    public LiveData<List<Chapter>> getChapters() {
        return null;
    }
    @Override
    public String[] getLanguages() {
        return new String[]{"en"};
    }
}
