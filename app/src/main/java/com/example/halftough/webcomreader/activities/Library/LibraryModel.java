package com.example.halftough.webcomreader.activities.Library;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import com.example.halftough.webcomreader.database.AppDatabase;
import com.example.halftough.webcomreader.database.ChaptersDAO;
import com.example.halftough.webcomreader.database.ReadWebcom;
import com.example.halftough.webcomreader.database.ReadWebcomsDAO;

import java.io.File;
import java.util.List;

public class LibraryModel extends AndroidViewModel {
    private ReadWebcomsDAO readWebcomsDAO;
    private ChaptersDAO chaptersDAO;
    private LiveData<List<ReadWebcom>> readWebcoms;

    public LibraryModel(Application application){
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        readWebcomsDAO = db.readWebcomsDAO();
        chaptersDAO = db.chaptersDAO();
        readWebcoms = readWebcomsDAO.getAll();
    }

    public LiveData<List<ReadWebcom>> getAllReadWebcoms() {
        return readWebcoms;
    }

    public void insert(ReadWebcom readWebcom){
        new insertAsyncTask(readWebcomsDAO).execute(readWebcom);
    }

    public void deleteWebcom(String wid) {
        // TODO option to save internal or external
        File root = android.os.Environment.getExternalStorageDirectory();
        File dir = new File(root.getAbsolutePath()+"/webcom/"+wid);
        deleteDir(dir);
        new deleteWebcomAsyncTask(chaptersDAO, readWebcomsDAO).execute(wid);
    }

    //TODO something if can't remove file?
    private void deleteDir(File dir){
        if(dir.isDirectory()){
            for(File f : dir.listFiles()){
                deleteDir(f);
            }
        }
        dir.delete();
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

    private static class deleteWebcomAsyncTask extends AsyncTask<String, Void, Void> {
        private ChaptersDAO chaptersDao;
        private ReadWebcomsDAO webcomsDao;
        deleteWebcomAsyncTask(ChaptersDAO chaptersDao, ReadWebcomsDAO webcomsDao){
            this.chaptersDao = chaptersDao;
            this.webcomsDao = webcomsDao;
        }
        @Override
        protected Void doInBackground(String... strings) {
            webcomsDao.delete(strings[0]);
            chaptersDao.deleteWebcom(strings[0]);
            return null;
        }
    }
}
