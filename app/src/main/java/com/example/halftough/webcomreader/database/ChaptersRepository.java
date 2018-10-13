package com.example.halftough.webcomreader.database;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.Nullable;

import com.example.halftough.webcomreader.DownloaderService;
import com.example.halftough.webcomreader.PreferenceHelper;
import com.example.halftough.webcomreader.UserRepository;
import com.example.halftough.webcomreader.activities.ChapterList.ChapterPreferencesFragment;
import com.example.halftough.webcomreader.webcoms.Webcom;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class ChaptersRepository {
    private ChaptersDAO chaptersDAO;
    private ReadWebcomsDAO webcomsDAO;
    private MutableLiveData<List<Chapter>> chapters;
    private Webcom webcom;
    private Application application;
    private SharedPreferences preferences;

    public ChaptersRepository(Application application, final Webcom webcom){
        this.application = application;
        AppDatabase db = AppDatabase.getDatabase(application);
        chaptersDAO = db.chaptersDAO();
        webcomsDAO = db.readWebcomsDAO();
        chapters = new MutableLiveData<>();
        preferences = application.getSharedPreferences(ChapterPreferencesFragment.PREFERENCE_KEY_COMIC+webcom.getId(), Context.MODE_PRIVATE);
        this.webcom = webcom;
        final LiveData<List<Chapter>> dChapters;

        dChapters = getDatabaseChapters();
        dChapters.observeForever(new Observer<List<Chapter>>() {
            @Override
            public void onChanged(@Nullable List<Chapter> chaps) {
                dChapters.removeObserver(this);
                chapters.postValue(chaps);
            }
        });
    }

    public MutableLiveData<List<Chapter>> getChapters(){
        return chapters;
    }

    public void downloadChapter(Chapter chapter){
        chapter.setDownloadStatus(Chapter.DownloadStatus.DOWNLOADING);
        new setDownloadStatusAsyncTask(chaptersDAO, this).execute(chapter);
        DownloaderService.enqueueChapter(application, chapter);
    }

    public void deleteChapter(Chapter chapter){
        chapter.setDownloadStatus(Chapter.DownloadStatus.UNDOWNLOADED);
        new setDownloadStatusAsyncTask(chaptersDAO, this).execute(chapter);
        UserRepository.deleteChapter(chapter);
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
        final LiveData<List<Chapter>> dbChapters = getDatabaseChapters();
        dbChapters.observeForever(new Observer<List<Chapter>>() {
            @Override
            public void onChanged(@Nullable List<Chapter> chaps) {
                dbChapters.removeObserver(this);
                chapters.postValue(chaps);
                int readCount = 0;
                for(Chapter chapter : chaps){
                    if(chapter.getStatus() == Chapter.Status.READ){
                        readCount += 1;
                    }
                }
                updateReadChapters(readCount);
            }
        });
    }

    private LiveData<List<Chapter>> getDatabaseChapters(){
        String chapterOrder = preferences.getString("chapter_order", "global");
        chapterOrder = PreferenceHelper.getChapterOrder(application.getSharedPreferences(UserRepository.GLOBAL_PREFERENCES, Context.MODE_PRIVATE), webcom, chapterOrder);
        if(chapterOrder.equals("ascending")) {
            return chaptersDAO.getChapters(webcom.getId());
        }
        else{
            return chaptersDAO.getChaptersDesc(webcom.getId());
        }
    }

    public void markRead(Chapter chapter){
        chapter.setStatus(Chapter.Status.READ);
        new updateAsyncTask(chaptersDAO).execute(chapter);
        update();
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
        update();
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
        update();
    }

    public void markUnread(Chapter chapter){
        chapter.setStatus(Chapter.Status.UNREAD);
        new updateAsyncTask(chaptersDAO).execute(chapter);
        update();
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
        update();
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
        update();
    }

    public void markWebcomBeingRead() {
        new markWebcomReadAsyncTask(webcomsDAO).execute(webcom.getId());
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

    public static void setDownloadStatus(Chapter chapter, Chapter.DownloadStatus status, ChaptersDAO chaptersDAO){
        chapter.setDownloadStatus(status);
        new setDownloadStatusAsyncTask(chaptersDAO).execute(chapter);
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

    public void updateReadChapters(int count){
        ReadWebcom readWebcom = new ReadWebcom(webcom.getId());
        readWebcom.setReadChapters(count);
        new updateWebcomReadChapterCountAsyncTask(webcomsDAO).execute(readWebcom);
    }

    private static class updateWebcomReadChapterCountAsyncTask extends AsyncTask<ReadWebcom, Void, Void> {
        private ReadWebcomsDAO mAsyncTaskDao;
        updateWebcomReadChapterCountAsyncTask(ReadWebcomsDAO dao){
            mAsyncTaskDao = dao;
        }
        @Override
        protected Void doInBackground(ReadWebcom... readWebcoms) {
            mAsyncTaskDao.updateReadChapterCount(readWebcoms[0].getWid(), readWebcoms[0].getReadChapters());
            return null;
        }
    }

    private static class markWebcomReadAsyncTask extends AsyncTask<String, Void, Void> {
        private ReadWebcomsDAO mAsyncDao;
        markWebcomReadAsyncTask(ReadWebcomsDAO dao){ mAsyncDao = dao; }
        @Override
        protected Void doInBackground(String... readWebcoms) {
            Date date = new Date();
            mAsyncDao.setLastReadDate(readWebcoms[0], new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date));
            return null;
        }
    }

    public static void insertChapter(Chapter chapter, ChaptersDAO chaptersDAO){
        new insertChapterAsyncTask(chaptersDAO).execute(chapter);
    }

    public static void insertChapter(Chapter chapter, ChaptersDAO chaptersDAO, ReadWebcomsDAO readWebcomsDAO, DownloaderService downloaderService){
        new insertChapterAsyncTask(chaptersDAO, readWebcomsDAO, downloaderService).execute(chapter);
    }

    private static class insertChapterAsyncTask extends AsyncTask<Chapter, Void, Void>{
        private ChaptersDAO chaptersDAO;
        private ReadWebcomsDAO readWebcomsDAO;
        private WeakReference<DownloaderService> downloaderService;
        insertChapterAsyncTask(ChaptersDAO dao){ this(dao, null, null); }
        insertChapterAsyncTask(ChaptersDAO dao, ReadWebcomsDAO readWebcomsDAO, DownloaderService downloaderService){
            chaptersDAO = dao;
            this.readWebcomsDAO = readWebcomsDAO;
            this.downloaderService = new WeakReference<>(downloaderService);
        }
        @Override
        protected Void doInBackground(Chapter... chapters) {
            chaptersDAO.insert(chapters[0]);
            if(readWebcomsDAO != null){
                int count = chaptersDAO.getChaptersCount(chapters[0].getWid());
                readWebcomsDAO.updateChapterCount(chapters[0].getWid(), count);
            }
            if(downloaderService != null && downloaderService.get() != null)
                downloaderService.get().broadcastChapterUpdated(chapters[0]);
            return null;
        }
    }
}
