package com.example.halftough.webcomreader.activities.MyWebcoms;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import com.example.halftough.webcomreader.database.ReadWebcom;
import com.example.halftough.webcomreader.database.ReadWebcomsRepository;

import java.util.List;

public class LibraryModel extends AndroidViewModel {
    private ReadWebcomsRepository readWebcomsRepository;
    private LiveData<List<ReadWebcom>> allReadWebcoms;

    public LibraryModel(Application application){
        super(application);
        readWebcomsRepository = new ReadWebcomsRepository(application);
        allReadWebcoms = readWebcomsRepository.getReadWebcoms();
    }

    public LiveData<List<ReadWebcom>> getAllReadWebcoms() {
        return allReadWebcoms;
    }

    public void insert(ReadWebcom readWebcom){
        readWebcomsRepository.insertReadWebcom(readWebcom);
    }

    public void deleteWebcom(String wid) {
        readWebcomsRepository.deleteWebcom(wid);
    }
}
