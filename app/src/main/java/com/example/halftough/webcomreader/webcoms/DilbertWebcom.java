package com.example.halftough.webcomreader.webcoms;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import com.example.halftough.webcomreader.R;
import com.example.halftough.webcomreader.database.Chapter;

import java.util.List;

import retrofit2.Call;

public class DilbertWebcom extends Webcom {
    private MutableLiveData<Integer> chapterCount;
    public DilbertWebcom(){
        if(chapterCount==null){
            chapterCount = new MutableLiveData<>();
            chapterCount.setValue(new Integer(0));
        }
    }
    @Override
    public String getId(){ return "dilbert"; }
    @Override
    public String getTitle(){ return "Dilbert"; }
    @Override
    public String getDescription(){ return ""; }

    @Override
    public int getIcon() {
        return R.mipmap.dilbert_ico;
    }

    @Override
    public format getFormat() {
        return format.PAGES;
    }

    @Override
    public MutableLiveData<Integer> getChapterCount() {
        return chapterCount;
    }

    @Override
    public String[] getTags() {
        String[] tags =  {"funny"};
        return tags;
    }

    @Override
    public Call<ComicPage> getChapterMetaCall(String number) {
        return null;
    }

    @Override
    public List<String> getChapterList() {
        return null;
    }

    @Override
    public String[] getLanguages() {
        return new String[]{"en"};
    }

    @Override
    public void updateChapters(){

    }
}
