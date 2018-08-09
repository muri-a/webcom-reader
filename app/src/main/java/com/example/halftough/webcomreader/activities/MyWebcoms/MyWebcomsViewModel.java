package com.example.halftough.webcomreader.activities.MyWebcoms;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import com.example.halftough.webcomreader.database.ReadWebcoms;
import com.example.halftough.webcomreader.database.ReadWebcomsRepository;

import java.util.List;

public class MyWebcomsViewModel extends AndroidViewModel {
    private ReadWebcomsRepository readWebcomsRepository;
    private LiveData<List<ReadWebcoms>> allReadWebcoms;

    public MyWebcomsViewModel(Application application){
        super(application);
        readWebcomsRepository = new ReadWebcomsRepository(application);
        allReadWebcoms = readWebcomsRepository.getReadWebcoms();
    }

    public LiveData<List<ReadWebcoms>> getAllReadWebcoms() {
        return allReadWebcoms;
    }

    public void insert(ReadWebcoms readWebcom){
        readWebcomsRepository.insertReadWebcom(readWebcom);
    }
}
