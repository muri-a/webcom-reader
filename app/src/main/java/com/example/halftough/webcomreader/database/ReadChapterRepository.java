package com.example.halftough.webcomreader.database;

import android.Manifest;
import android.app.Activity;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.example.halftough.webcomreader.DownloaderService;
import com.example.halftough.webcomreader.PreferenceHelper;
import com.example.halftough.webcomreader.TaskDelegate;
import com.example.halftough.webcomreader.activities.ChapterList.ChapterPreferencesFragment;
import com.example.halftough.webcomreader.activities.ReadChapter.ComicPageView;
import com.example.halftough.webcomreader.activities.ReadChapter.ReadChapterActivity;
import com.example.halftough.webcomreader.webcoms.Webcom;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;

//TODO Move everythong. Repository doesn't fif here
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
                DownloaderService.enqueueChapter(context, chapter.getValue(), DownloaderService.DownoladType.ONREAD);
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
        Log.e("Marking read", "1");
        if(chapter.getValue() == null) {
            return;
        }
        Log.e("Marking read", "2");
        wasUpdate = true;
        chapter.getValue().setStatus(Chapter.Status.READ);
        new setStatusAsyncTask(chaptersDAO, new TaskDelegate() {
            @Override
            public void onFinish() {
                DownloaderService.autodownload(context, webcom.getId());
                DownloaderService.autoremove(context, webcom.getId());
            }
        }).execute(chapter.getValue());
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

    private static class setStatusAsyncTask extends AsyncTask<Chapter, Void, Void> {

        private ChaptersDAO mAsyncTaskDao;
        private TaskDelegate taskDelegate;
        setStatusAsyncTask(ChaptersDAO dao){ this(dao, null); }
        setStatusAsyncTask(ChaptersDAO dao, TaskDelegate delegate) {
            mAsyncTaskDao = dao;
            taskDelegate = delegate;
        }
        @Override
        protected Void doInBackground(Chapter...chapters) {
            mAsyncTaskDao.setStatus(chapters[0].getWid(), chapters[0].getChapter(), chapters[0].getStatus());
            if(taskDelegate != null){
                taskDelegate.onFinish();
            }
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
            saveLastRead(chapter.getChapter());
        }
    }

    private void saveLastRead(String c) {
        SharedPreferences chapterPreferences = context.getSharedPreferences(ChapterPreferencesFragment.PREFERENCE_KEY_COMIC+webcom.getId(), Context.MODE_PRIVATE);
        int toSave = PreferenceHelper.getAutoremoveSave(context, webcom.getId());
        String last = chapterPreferences.getString("last_list", "[]");
        try {
            JSONArray jarr = new JSONArray(last);
            //Handle if chapter was read recently
            for(int i=0; i<jarr.length(); i++){
                if( jarr.getString(i).equals(c) ){
                    jarr.remove(i--);
                }
            }
            //Remove excess
            while(jarr.length() >= toSave){
                jarr.remove(0);
            }
            jarr.put(c);
            chapterPreferences.edit().putString("last_list", jarr.toString()).apply();
        } catch (JSONException e) {
            JSONArray jarr = new JSONArray();
            jarr.put(c);
            chapterPreferences.edit().putString("last_list", jarr.toString()).apply();
        }
    }
}
