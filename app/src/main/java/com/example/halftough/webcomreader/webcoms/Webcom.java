package com.example.halftough.webcomreader.webcoms;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.graphics.drawable.Drawable;

import com.example.halftough.webcomreader.database.Chapter;

import java.util.List;

import retrofit2.Call;

public abstract class Webcom {

    public enum format { CHAPTERS, PAGES }

    public abstract String getId();
    public abstract String getTitle();
    public abstract String getDescription();
    public abstract int getIcon();
    public abstract format getFormat();
    public abstract MutableLiveData<Integer> getChapterCount(); //Returns number of all available pages/chapters of comic
    public abstract String[] getTags();
    public abstract Call<ComicPage> getPageCall(String number);
    public abstract List<String> getChapterList();
    public abstract String[] getLanguages();
}
