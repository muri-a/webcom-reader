package com.example.halftough.webcomreader.database;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ImageView;


import com.example.halftough.webcomreader.activities.ReadChapter.ComicPageView;
import com.example.halftough.webcomreader.webcoms.ComicPage;
import com.example.halftough.webcomreader.webcoms.Webcom;
import com.squareup.picasso.Picasso;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReadChapterRepository {
    private ChaptersDAO chaptersDAO;
    Webcom webcom;
    //String number;
    LiveData<Chapter> chapter;
    ComicPageView imageView;
    boolean wasUpdate = false;
    public ReadChapterRepository(Application application, Webcom webcom, ComicPageView imageView) {
        AppDatabase db = AppDatabase.getDatabase(application);
        chaptersDAO = db.chaptersDAO();
        this.webcom = webcom;
        this.imageView = imageView;
    }

    public void setChapter(String c){
        //number = c;
        chapter = chaptersDAO.getChapter(webcom.getId(), c);
        chapter.observeForever(new Observer<Chapter>() {
            @Override
            public void onChanged(@Nullable Chapter c) {
                chapter.removeObserver(this);
                getImage();
            }
        });
    }

    // TODO Only mark chapter as read if it wasn't marked before (and image was downloaded before user changed page (?))
    public void getImage() {
        Call<ComicPage> call = webcom.getPageCall(chapter.getValue().getChapter());
        call.enqueue(new Callback<ComicPage>() {
            @Override
            public void onResponse(Call<ComicPage> call, Response<ComicPage> response) {
                Picasso.get().load(response.body().getImg()).fit().centerInside().into(imageView, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        imageView.setX(0);
                        //If chapter haven't been loaded yet, we put method in the observer
                        if(chapter.getValue() != null){
                            markRead();
                        }
                        else{
                            chapter.observeForever(new Observer<Chapter>() {
                                @Override
                                public void onChanged(@Nullable Chapter chap) {
                                    markRead();
                                    chapter.removeObserver(this);
                                }
                            });
                        }
                    }
                    @Override
                    public void onError(Exception e) {
                        Log.e("DOWNLOADING_ERROR", e.toString());
                    }
                });
            }
            @Override
            public void onFailure(Call<ComicPage> call, Throwable t) {

            }
        });

    }

    public void markRead(){
        wasUpdate = true;
        chapter.getValue().setStatus(Chapter.Status.READ);
        new updateAsyncTask(chaptersDAO).execute(chapter.getValue());
    }

    public boolean getUpdateMarker() {
        return wasUpdate;
    }

    public void nextChapter() {
        chapter = chaptersDAO.getNext(webcom.getId(), chapter.getValue().getChapter());
        chapter.observeForever(new Observer<Chapter>() {
            @Override
            public void onChanged(@Nullable Chapter c) {
                chapter.removeObserver(this);
                getImage();
            }
        });
    }

    public void previousChapter() {
        chapter = chaptersDAO.getPrevious(webcom.getId(), chapter.getValue().getChapter());
        chapter.observeForever(new Observer<Chapter>() {
            @Override
            public void onChanged(@Nullable Chapter c) {
                chapter.removeObserver(this);
                getImage();
            }
        });
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
}
