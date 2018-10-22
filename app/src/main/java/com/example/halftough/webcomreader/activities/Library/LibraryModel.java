package com.example.halftough.webcomreader.activities.Library;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.example.halftough.webcomreader.UserRepository;
import com.example.halftough.webcomreader.database.AppDatabase;
import com.example.halftough.webcomreader.database.ChaptersDAO;
import com.example.halftough.webcomreader.database.ReadWebcom;
import com.example.halftough.webcomreader.database.ReadWebcomsDAO;
import com.example.halftough.webcomreader.webcoms.Webcom;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class LibraryModel extends AndroidViewModel {
    enum SortBy { TITLE(0), TITLE_DEC(1), READ(2), READ_DEC(3), UPDATED(4), UPDATED_DEC(5), UNREAD(6), UNREAD_DEC(7);
        private int num;
        private SortBy(int a){ num = a;}
        public int toInt(){ return num; }
        public SortBy reverse(){
            if(num%2 == 0)
                return fromInt(num+1);
            else
                return fromInt(num-1);
        }
        static SortBy fromInt(int num){
            switch(num){
                default:
                case 0: return TITLE;
                case 1: return TITLE_DEC;
                case 2: return READ;
                case 3: return READ_DEC;
                case 4: return UPDATED;
                case 5: return UPDATED_DEC;
                case 6: return UNREAD;
                case 7: return UNREAD_DEC;
            }
        }
    }

    private ReadWebcomsDAO readWebcomsDAO;
    private ChaptersDAO chaptersDAO;
    private LiveData<List<ReadWebcom>> readWebcoms;
    private SortBy sortBy;

    public LibraryModel(Application application){
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        readWebcomsDAO = db.readWebcomsDAO();
        chaptersDAO = db.chaptersDAO();
        readWebcoms = readWebcomsDAO.getAll();
    }

    public void sort(){
        SharedPreferences preferences = preferences = getApplication().getSharedPreferences(UserRepository.GLOBAL_PREFERENCES, MODE_PRIVATE);
        sortBy = SortBy.fromInt(preferences.getInt(LibraryActivity.SORTING_KEY, 0));
        if(readWebcoms.getValue() != null)
            Collections.sort(readWebcoms.getValue(), new Comparator<ReadWebcom>() {
                @Override
                public int compare(ReadWebcom o1, ReadWebcom o2) {
                    int dec = 1;
                    switch(sortBy){
                        case TITLE:
                        case TITLE_DEC:
                            Webcom webcom1 = UserRepository.getWebcomInstance(o1.getWid());
                            Webcom webcom2 = UserRepository.getWebcomInstance(o2.getWid());
                            if(sortBy == SortBy.TITLE_DEC)
                                dec = -1;
                            return dec*webcom1.getTitle().compareTo(webcom2.getTitle());
                        case READ:
                        case READ_DEC: {
                            Date d1, d2;
                            try {
                                d1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(o1.getLastRead());
                            } catch (ParseException | NullPointerException e) {
                                d1 = new Date(0);
                            }
                            try {
                                d2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(o2.getLastRead());
                            } catch (ParseException | NullPointerException e) {
                                d2 = new Date(0);
                            }
                            if (sortBy == SortBy.READ_DEC)
                                dec = -1;
                            return dec * d1.compareTo(d2);
                        }
                        case UPDATED:
                        case UPDATED_DEC: {
                            Date d1, d2;
                            try {
                                d1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(o1.getLastUpdated());
                            } catch (ParseException | NullPointerException e) {
                                d1 = new Date(0);
                            }
                            try {
                                d2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(o2.getLastUpdated());
                            } catch (ParseException | NullPointerException e) {
                                d2 = new Date(0);
                            }
                            if (sortBy == SortBy.UPDATED_DEC)
                                dec = -1;
                            return dec * d1.compareTo(d2);
                        }
                        case UNREAD:
                        case UNREAD_DEC:
                            int u1 = o1.getChapterCount() - o1.getReadChapters();
                            int u2 = o2.getChapterCount() - o2.getReadChapters();
                            if(sortBy == SortBy.UNREAD_DEC)
                                dec = -1;
                            return dec*(u2-u1);
                    }
                    return 0;
                }
            });
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
        if(dir.exists() && dir.isDirectory() && dir.listFiles() != null){
            for(File f : dir.listFiles()){
                deleteDir(f);
            }
        }
        dir.delete();
    }

    //TODO clean this up

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
