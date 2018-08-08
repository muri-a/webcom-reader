package com.example.halftough.webcomreader.database;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.List;

public class ReadWebcomsRepository {
    private ReadWebcomsDAO readWebcomsDAO;
    private LiveData<List<ReadWebcoms>> readWebcoms;

    public ReadWebcomsRepository(Application application){
        AppDatabase db = AppDatabase.getDatabase(application);
        readWebcomsDAO = db.readWebcomsDAO();
        readWebcoms = readWebcomsDAO.getAll();
    }

    public LiveData<List<ReadWebcoms>> getReadWebcoms() {
        return readWebcoms;
    }

    public void insertReadWebcom(ReadWebcoms readWebcom){
        new insertAsyncTask(readWebcomsDAO).execute(readWebcom);
    }

    private static class insertAsyncTask extends AsyncTask<ReadWebcoms, Void, Void> {

        private ReadWebcomsDAO mAsyncTaskDao;

        insertAsyncTask(ReadWebcomsDAO dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final ReadWebcoms... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }
}
