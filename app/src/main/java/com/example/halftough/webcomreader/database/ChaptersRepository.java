package com.example.halftough.webcomreader.database;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.os.AsyncTask;
import android.support.annotation.Nullable;

import com.example.halftough.webcomreader.DownloaderService;
import com.example.halftough.webcomreader.NoWebcomClassException;
import com.example.halftough.webcomreader.OneByOneCallDownloader;
import com.example.halftough.webcomreader.UserRepository;
import com.example.halftough.webcomreader.webcoms.ComicPage;
import com.example.halftough.webcomreader.webcoms.Webcom;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

import retrofit2.Call;
import retrofit2.Response;

public class ChaptersRepository {
    private ChaptersDAO chaptersDAO;
    private MutableLiveData<List<Chapter>> chapters;
    private Webcom webcom;
    private Application application;

    public ChaptersRepository(Application application, final String wid){
        this.application = application;
        AppDatabase db = AppDatabase.getDatabase(application);
        chaptersDAO = db.chaptersDAO();
        chapters = new MutableLiveData<>();
        try {
            webcom = UserRepository.getWebcomInstance(wid);
            final LiveData<List<Chapter>> dChapters = chaptersDAO.getChapters(wid);
            dChapters.observeForever(new Observer<List<Chapter>>() {
                @Override
                public void onChanged(@Nullable List<Chapter> chaps) {
                    dChapters.removeObserver(this);
                    chapters.postValue(chaps);
                }
            });
        } catch (NoWebcomClassException e) {
            e.printStackTrace();
        }
    }

    public MutableLiveData<List<Chapter>> getChapters(){
        return chapters;
    }

    //TODO? might remove it
    public void insertChapter(Chapter chapter){
        new insertAsyncTask(chaptersDAO).execute(chapter);
    }

    public void downloadChapter(Chapter chapter){
        chapter.setDownloadStatus(Chapter.DownloadStatus.DOWNLOADING);
        new setDownloadStatusAsyncTask(chaptersDAO, this).execute(chapter);
        DownloaderService.enqueueChapter(application, chapter);
    }

    public Chapter getChapterToRead() {
        if(chapters.getValue()!=null){
            for(Chapter chapter : chapters.getValue()){
                if(chapter.getStatus() == Chapter.Status.UNREAD){
                    return chapter;
                }
            }
        }
        return null;
    }

    public List<Chapter> getChaptersToDownload(int number) {
        List<Chapter> toDownload = new ArrayList<>();
        if(chapters.getValue()!=null){
            for(Chapter chapter: chapters.getValue()){
                if(chapter.getStatus() == Chapter.Status.UNREAD && chapter.getDownloadStatus() == Chapter.DownloadStatus.UNDOWNLOADED){
                    toDownload.add(chapter);
                    if(toDownload.size() >= number){
                        return toDownload;
                    }
                }
            }
        }
        return toDownload;
    }

    public void update() {
        final LiveData<List<Chapter>> dbChapters = chaptersDAO.getChapters(webcom.getId());
        dbChapters.observeForever(new Observer<List<Chapter>>() {
            @Override
            public void onChanged(@Nullable List<Chapter> chaps) {
                dbChapters.removeObserver(this);
                chapters.postValue(chaps);
            }
        });
    }

    private static class insertAsyncTask extends AsyncTask<Chapter, Void, Void> {
        private ChaptersDAO mAsyncTaskDao;
        insertAsyncTask(ChaptersDAO dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Chapter... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    public void markRead(Chapter chapter){
        chapter.setStatus(Chapter.Status.READ);
        new updateAsyncTask(chaptersDAO).execute(chapter);
    }

    public void markReadTo(Chapter chapter){
        Iterator<Chapter> it = chapters.getValue().iterator();
        List<Chapter> toUpdate = new ArrayList<>();
        while(it.hasNext()){
            Chapter c = it.next();
            if(c.compareTo(chapter) <= 0 && c.getStatus()!= Chapter.Status.READ){
                c.setStatus(Chapter.Status.READ);
                toUpdate.add(c);
            }
        }
        if(toUpdate.size()>0)
            new updateListAsyncTask(chaptersDAO).execute(toUpdate);
    }

    public void markReadFrom(Chapter chapter){
        ListIterator<Chapter> it = chapters.getValue().listIterator(chapters.getValue().size());
        List<Chapter> toUpdate = new ArrayList<>();
        while(it.hasPrevious()){
            Chapter c = it.previous();
            if(c.compareTo(chapter) >= 0 && c.getStatus()!= Chapter.Status.READ){
                c.setStatus(Chapter.Status.READ);
                toUpdate.add(c);
            }
        }
        if(toUpdate.size()>0)
            new updateListAsyncTask(chaptersDAO).execute(toUpdate);
    }

    public void markUnread(Chapter chapter){
        chapter.setStatus(Chapter.Status.UNREAD);
        new updateAsyncTask(chaptersDAO).execute(chapter);
    }

    public void markUnreadTo(Chapter chapter){
        Iterator<Chapter> it = chapters.getValue().iterator();
        List<Chapter> toUpdate = new ArrayList<>();
        while(it.hasNext()){
            Chapter c = it.next();
            if(c.compareTo(chapter) <= 0 && c.getStatus()!= Chapter.Status.UNREAD){
                c.setStatus(Chapter.Status.UNREAD);
                toUpdate.add(c);
            }
        }
        if(toUpdate.size()>0)
            new updateListAsyncTask(chaptersDAO).execute(toUpdate);
    }

    public void markUnreadFrom(Chapter chapter){
        ListIterator<Chapter> it = chapters.getValue().listIterator(chapters.getValue().size());
        List<Chapter> toUpdate = new ArrayList<>();
        while(it.hasPrevious()){
            Chapter c = it.previous();
            if(c.compareTo(chapter) >= 0 && c.getStatus()!= Chapter.Status.UNREAD){
                c.setStatus(Chapter.Status.UNREAD);
                toUpdate.add(c);
            }
        }
        if(toUpdate.size()>0)
            new updateListAsyncTask(chaptersDAO).execute(toUpdate);
    }

    private static class updateAsyncTask extends AsyncTask<Chapter, Void, Void>{
        private ChaptersDAO mAsyncTaskDao;
        updateAsyncTask(ChaptersDAO dao) {
            mAsyncTaskDao = dao;
        }
        @Override
        protected Void doInBackground(Chapter... chapters) {
            mAsyncTaskDao.update(chapters[0]);
            return null;
        }
    }

    private class updateListAsyncTask extends AsyncTask<List<Chapter>, Void, Void>{
        private ChaptersDAO mAsyncTaskDao;
        updateListAsyncTask(ChaptersDAO dao){ mAsyncTaskDao = dao; }
        @Override
        protected Void doInBackground(List<Chapter>... lists) {
            mAsyncTaskDao.update(lists[0]);
            return null;
        }
    }

    public static class setDownloadStatusAsyncTask extends AsyncTask<Chapter, Void, Void>{
        private ChaptersDAO mAsyncTaskDao;
        private ChaptersRepository repository;
        public setDownloadStatusAsyncTask(ChaptersDAO dao){ this(dao, null); }
        public setDownloadStatusAsyncTask(ChaptersDAO dao, ChaptersRepository repository) {
            mAsyncTaskDao = dao;
            this.repository = repository;
        }
        @Override
        protected Void doInBackground(Chapter... chapters) {
            mAsyncTaskDao.setDownloadStatus(chapters[0].getWid(), chapters[0].getChapter(), chapters[0].getDownloadStatus());
            if(repository!=null)
                repository.update();
            return null;
        }
    }
}
