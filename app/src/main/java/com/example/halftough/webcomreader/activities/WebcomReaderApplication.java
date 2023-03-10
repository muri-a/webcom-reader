package com.example.halftough.webcomreader.activities;

import android.app.Application;
import android.os.AsyncTask;

import com.example.halftough.webcomreader.DownloaderService;
import com.example.halftough.webcomreader.UpdateWebcomsService;
import com.example.halftough.webcomreader.database.AppDatabase;
import com.example.halftough.webcomreader.database.ChaptersDAO;

public class WebcomReaderApplication extends Application {

    @Override
    public void onCreate(){
        super.onCreate();

        //If for some reason downloading was interrupted but application didn't get to makr it, do it now
        AppDatabase db = AppDatabase.getDatabase(this);
        new AsyncUndownloadedClearer(db.chaptersDAO()).execute();

        UpdateWebcomsService.updateNewChapters(this);
    }

    private static class AsyncUndownloadedClearer extends AsyncTask<Void, Void, Void>{
        ChaptersDAO DAO;
        AsyncUndownloadedClearer(ChaptersDAO dao){DAO = dao;}
        @Override
        protected Void doInBackground(Void... voids) {
            DAO.clearUndownloaded();
            return null;
        }
    }
}
