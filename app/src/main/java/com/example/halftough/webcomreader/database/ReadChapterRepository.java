package com.example.halftough.webcomreader.database;

import android.app.Activity;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;


import com.example.halftough.webcomreader.activities.ReadChapter.ComicPageView;
import com.example.halftough.webcomreader.webcoms.ComicPage;
import com.example.halftough.webcomreader.webcoms.Webcom;
import com.squareup.picasso.Picasso;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReadChapterRepository {
    private ChaptersDAO chaptersDAO;
    private Webcom webcom;
    private LiveData<Chapter> firstChapter, chapter, lastChapter;
    private ComicPageView imageView;
    private boolean wasUpdate = false;
    private Activity context;

    public ReadChapterRepository(Activity context, Webcom webcom, ComicPageView imageView) {
        AppDatabase db = AppDatabase.getDatabase(context.getApplicationContext());
        chaptersDAO = db.chaptersDAO();
        this.webcom = webcom;
        this.imageView = imageView;
        this.context = context;
    }

    public void setChapter(String c){
        chapter = chaptersDAO.getChapter(webcom.getId(), c);
        chapter.observeForever(new ChapterChangedObserver(chapter, context));
        firstChapter = chaptersDAO.getFirstChapter();
        firstChapter.observeForever(new Observer<Chapter>() {
            @Override
            public void onChanged(@Nullable Chapter chapter) {
                firstChapter.removeObserver(this);
                imageView.setFirstChapterId(chapter.getChapter());
            }
        });
        lastChapter = chaptersDAO.getLastChapter();
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
        Call<ComicPage> call = webcom.getChapterMetaCall(chapter.getValue().getChapter());
        call.enqueue(new Callback<ComicPage>() {
            @Override
            public void onResponse(Call<ComicPage> call, Response<ComicPage> response) {
                Picasso.get().load(response.body().getUrl()).into(imageView);
            }
            @Override
            public void onFailure(Call<ComicPage> call, Throwable t) {

            }
        });
    }

    public void markRead(){
        if(chapter.getValue() == null)
            return;
        wasUpdate = true;
        chapter.getValue().setStatus(Chapter.Status.READ);
        new updateAsyncTask(chaptersDAO).execute(chapter.getValue());
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
            markRead();
        }
    }
}
