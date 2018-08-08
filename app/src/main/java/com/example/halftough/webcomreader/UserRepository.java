package com.example.halftough.webcomreader;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import com.example.halftough.webcomreader.database.AppDatabase;
import com.example.halftough.webcomreader.database.ReadWebcoms;
import com.example.halftough.webcomreader.database.ReadWebcomsDAO;
import com.example.halftough.webcomreader.webcoms.DilbertWebcom;
import com.example.halftough.webcomreader.webcoms.Webcom;
import com.example.halftough.webcomreader.webcoms.XkcdWebcom;

import java.util.List;

public class UserRepository {

    static public Webcom getWebcomInstance(String id) throws NoWebcomClassException {
        switch(id){
            case "dilbert":
                return new DilbertWebcom();
            case "xkcd":
                return new XkcdWebcom();
            default:
                throw new NoWebcomClassException(id);
        }
    }

}
