package com.example.halftough.webcomreader.webcoms;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.graphics.drawable.Drawable;

import com.example.halftough.webcomreader.database.Chapter;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class Webcom {

    public enum format { CHAPTERS, PAGES }

    public abstract String getId();
    public abstract String getTitle();
    public abstract String getDescription();
    public abstract int getIcon();
    public abstract format getFormat();
    public abstract MutableLiveData<Integer> getChapterCount(); //Returns number of all available pages/chapters of comic
    public abstract String[] getTags();
    public LiveData<String> getChapterUrl(String chapter){
        final MutableLiveData<String> chapterUrl = new MutableLiveData<>();
        Call<ComicPage> call = getChapterMetaCall(chapter);
        call.enqueue(new Callback<ComicPage>() {
            @Override
            public void onResponse(Call<ComicPage> call, Response<ComicPage> response) {
                chapterUrl.setValue(response.body().getUrl());
            }

            @Override
            public void onFailure(Call<ComicPage> call, Throwable t) {
                chapterUrl.setValue("");
            }
        });
        return chapterUrl;
    }
    public abstract Call<ComicPage> getChapterMetaCall(String number);
    public abstract List<String> getChapterList();
    public abstract String[] getLanguages();
}
