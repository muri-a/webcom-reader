package com.example.halftough.webcomreader.database;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.List;

public class ReadWebcomsRepository {
    private ReadWebcomsDAO readWebcomsDAO;
    private LiveData<List<ReadWebcom>> readWebcoms;

    public ReadWebcomsRepository(Application application){
        AppDatabase db = AppDatabase.getDatabase(application);
        readWebcomsDAO = db.readWebcomsDAO();
        readWebcoms = readWebcomsDAO.getAll();
    }

    public LiveData<List<ReadWebcom>> getReadWebcoms() {
        return readWebcoms;
    }

    public void insertReadWebcom(ReadWebcom readWebcom){
        new insertAsyncTask(readWebcomsDAO).execute(readWebcom);
    }

    private static class insertAsyncTask extends AsyncTask<ReadWebcom, Void, Void> {

        private ReadWebcomsDAO mAsyncTaskDao;

        insertAsyncTask(ReadWebcomsDAO dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final ReadWebcom... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }
}
