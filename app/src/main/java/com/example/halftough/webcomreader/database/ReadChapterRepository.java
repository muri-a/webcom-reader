package com.example.halftough.webcomreader.database;

import android.Manifest;
import android.app.Activity;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.example.halftough.webcomreader.DownloaderService;
import com.example.halftough.webcomreader.activities.ReadChapter.ComicPageView;
import com.example.halftough.webcomreader.activities.ReadChapter.ReadChapterActivity;
import com.example.halftough.webcomreader.webcoms.Webcom;
import com.squareup.picasso.Picasso;

import java.io.File;

public class ReadChapterRepository {
    private ChaptersDAO chaptersDAO;
    private Webcom webcom;
    private LiveData<Chapter> firstChapter, chapter, lastChapter;
    private ComicPageView imageView;
    private boolean wasUpdate = false;
    private ReadChapterActivity context;

    public ReadChapterRepository(ReadChapterActivity context, Webcom webcom, ComicPageView imageView) {
        AppDatabase db = AppDatabase.getDatabase(context.getApplicationContext());
        chaptersDAO = db.chaptersDAO();
        this.webcom = webcom;
        this.imageView = imageView;
        this.context = context;
    }

    public void setChapter(String c){
        chapter = chaptersDAO.getChapter(webcom.getId(), c);
        chapter.observeForever(new ChapterChangedObserver(chapter, context));
        firstChapter = chaptersDAO.getFirstChapter(webcom.getId());
        firstChapter.observeForever(new Observer<Chapter>() {
            @Override
            public void onChanged(@Nullable Chapter chapter) {
                firstChapter.removeObserver(this);
                imageView.setFirstChapterId(chapter.getChapter());
            }
        });
        lastChapter = chaptersDAO.getLastChapter(webcom.getId());
        lastChapter.observeForever(new Observer<Chapter>() {
            @Override
            public void onChanged(@Nullable Chapter chapter) {
                lastChapter.removeObserver(this);
                imageView.setLastChapterId(chapter.getChapter());
            }
        });
    }

    private void getImage() {
        if(chapter.getValue()==null)
            return;
        switch (chapter.getValue().getDownloadStatus()){
            case DOWNLOADED:
                getImageFromStorage();
                break;
            case UNDOWNLOADED:
                if( ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                }
                context.showDownloadingText();
                wasUpdate = true;
                Chapter lChapter = chapter.getValue();
                lChapter.setDownloadStatus(Chapter.DownloadStatus.DOWNLOADING);
                new ChaptersRepository.setDownloadStatusAsyncTask(chaptersDAO).execute(lChapter);
                DownloaderService.enqueueChapter(context, chapter.getValue());
                //no break
            case DOWNLOADING:
                context.listenForDownload(chapter.getValue());
        }
    }

    public void getImageFromStorage(){
        if(chapter.getValue() == null)
            return;
        File f = chapter.getValue().getFile();
        if(f != null) {
            context.hideDownloadingText();
            Picasso.get().load(f).into(imageView);
            markRead();
        }
        else{
            chapter = chaptersDAO.getChapter(chapter.getValue().getWid(), chapter.getValue().getChapter());
            chapter.observe(context, new Observer<Chapter>() {
                @Override
                public void onChanged(@Nullable Chapter chapter) {
                    ReadChapterRepository.this.chapter.removeObserver(this);
                    if(chapter.getDownloadStatus() == Chapter.DownloadStatus.UNDOWNLOADED){
                        context.showCouldntDownloadText();
                    }
                }
            });
        }
    }

    public void markRead(){
        if(chapter.getValue() == null)
            return;
        wasUpdate = true;
        chapter.getValue().setStatus(Chapter.Status.READ);
        new setStatusAsyncTask(chaptersDAO).execute(chapter.getValue());
    }

    public boolean getUpdateMarker() {
        return wasUpdate;
    }

    public void nextChapter() {
        chapter = chaptersDAO.getNext(webcom.getId(), chapter.getValue().getChapter());
        chapter.observeForever(new ChapterChangedObserver(chapter, context));
    }

    public void previousChapter() {
        chapter = chaptersDAO.getPrevious(webcom.getId(), chapter.getValue().getChapter());
        chapter.observeForever(new ChapterChangedObserver(chapter, context));
    }

    public String getChapterNumber() {
        return chapter.getValue()!=null?chapter.getValue().getChapter():null;
    }

    //TODO remove?
    private static class updateAsyncTask extends AsyncTask<Chapter, Void, Void> {
        private ChaptersDAO mAsyncTaskDao;
        updateAsyncTask(ChaptersDAO dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(Chapter...chapters) {
            mAsyncTaskDao.update(chapters[0]);
            return null;
        }
    }

    private static class setStatusAsyncTask extends AsyncTask<Chapter, Void, Void> {
        private ChaptersDAO mAsyncTaskDao;
        setStatusAsyncTask(ChaptersDAO dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(Chapter...chapters) {
            mAsyncTaskDao.setStatus(chapters[0].getWid(), chapters[0].getChapter(), chapters[0].getStatus());
            return null;
        }
    }

    private class ChapterChangedObserver implements Observer<Chapter>{
        private final LiveData<Chapter> chapter;
        private final Activity context;
        public ChapterChangedObserver(LiveData<Chapter> chapter, Activity context){
            this.chapter = chapter;
            this.context = context;
        }
        @Override
        public void onChanged(@Nullable Chapter chapter) {
            this.chapter.removeObserver(this);
            context.setTitle(chapter.getTitle());
            imageView.setCurrentChapter(chapter.getChapter());
            getImage();
        }
    }
}
