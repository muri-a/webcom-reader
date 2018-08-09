package com.example.halftough.webcomreader.database;

import android.app.Application;
import android.arch.lifecycle.LiveData;

import com.example.halftough.webcomreader.NoWebcomClassException;
import com.example.halftough.webcomreader.UserRepository;
import com.example.halftough.webcomreader.webcoms.Webcom;

import java.util.List;

public class ChaptersRepository {
    private LiveData<List<Chapter>> chapters;

    //TODO save data in database
    //Application for later when we get data from database
    public ChaptersRepository(Application application, String wid){
        try {
            Webcom webcom = UserRepository.getWebcomInstance(wid);
            chapters = webcom.getChapters();
        } catch (NoWebcomClassException e) {
            e.printStackTrace();
        }
    }

    public LiveData<List<Chapter>> getChapters(){
        return chapters;
    }
}
