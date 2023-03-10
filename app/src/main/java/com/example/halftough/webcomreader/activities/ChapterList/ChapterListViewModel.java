package com.example.halftough.webcomreader.activities.ChapterList;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.SharedPreferences;

import com.example.halftough.webcomreader.PreferenceHelper;
import com.example.halftough.webcomreader.UserRepository;
import com.example.halftough.webcomreader.database.Chapter;
import com.example.halftough.webcomreader.database.ChaptersRepository;
import com.example.halftough.webcomreader.webcoms.Webcom;
import com.google.common.collect.Lists;

import java.util.List;

public class ChapterListViewModel extends AndroidViewModel {
    private ChaptersRepository chaptersRepository;
    private MutableLiveData<List<Chapter>> chapters;
    private Application application;
    private SharedPreferences preferences;
    private Webcom webcom;

    public ChapterListViewModel(Application application) {
        super(application);
        this.application = application;
    }

    public void setWid(String wid){
        webcom = UserRepository.getWebcomInstance(wid);
        chaptersRepository = new ChaptersRepository(application, webcom);
        chapters = chaptersRepository.getChapters();
        preferences = application.getSharedPreferences(ChapterPreferencesFragment.PREFERENCE_KEY_COMIC+wid, Context.MODE_PRIVATE);
    }

    public Chapter getChapterToRead() {
        return chaptersRepository.getChapterToRead();
    }

    public void changeOrder(){
        if(chapters.getValue() == null)
            return;
        chapters.postValue(Lists.reverse(chapters.getValue()));
        String pref = preferences.getString("chapter_order", "global");
        pref = PreferenceHelper.getChapterOrder(application.getSharedPreferences(UserRepository.GLOBAL_PREFERENCES, Context.MODE_PRIVATE), webcom, pref);
        if (pref.equals("ascending")) {
            preferences.edit().putString("chapter_order", "decreasing").apply();
        }
        else{
            preferences.edit().putString("chapter_order", "ascending").apply();
        }
    }

    public void downloadNextChapters(int number) {
        List<Chapter> chapters = chaptersRepository.getChaptersToDownload(number);
        for (Chapter chapter : chapters) {
            downloadChapter(chapter);
        }
    }

    public LiveData<List<Chapter>> getChapters() {
        return chapters;
    }

    public void downloadChapter(Chapter chapter){
        chaptersRepository.downloadChapter(chapter);
    }

    public void deleteChapter(Chapter chapter){
        chaptersRepository.deleteChapter(chapter);
    }

    public void update() {
        chaptersRepository.update();
    }

    public void markRead(Chapter chapter){
        chaptersRepository.markRead(chapter);
    }

    public void markReadTo(Chapter chapter){
        chaptersRepository.markReadTo(chapter);
    }

    public void markReadFrom(Chapter chapter){
        chaptersRepository.markReadFrom(chapter);
    }

    public void markUnread(Chapter chapter){
        chaptersRepository.markUnread(chapter);
    }

    public void markUnreadTo(Chapter chapter){
        chaptersRepository.markUnreadTo(chapter);
    }

    public void markUnreadFrom(Chapter chapter){
        chaptersRepository.markUnreadFrom(chapter);
    }

    public void markWebcomBeingRead() {
        chaptersRepository.markWebcomBeingRead();
    }
}
