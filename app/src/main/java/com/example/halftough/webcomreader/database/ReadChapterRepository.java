package com.example.halftough.webcomreader.database;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ImageView;


import com.example.halftough.webcomreader.webcoms.ComicPage;
import com.example.halftough.webcomreader.webcoms.Webcom;
import com.squareup.picasso.Picasso;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReadChapterRepository {
    private ChaptersDAO chaptersDAO;
    Webcom webcom;
    LiveData<Chapter> chapter;
    boolean wasUpdate = false;
    public ReadChapterRepository(Application application, Webcom webcom, String number) {
        AppDatabase db = AppDatabase.getDatabase(application);
        chaptersDAO = db.chaptersDAO();
        this.webcom = webcom;
        chapter =  chaptersDAO.getChapter(webcom.getId(), number);
    }

    // TODO Only mark chapter as read if it wasn't marked before (and image was downloaded before user changed page (?))
    public void getImageFor(final String number, final ImageView readChapterImage) {
        Call<ComicPage> call = webcom.getPageCall(number);
        call.enqueue(new Callback<ComicPage>() {
            @Override
            public void onResponse(Call<ComicPage> call, Response<ComicPage> response) {
                Picasso.get().load(response.body().getImg()).fit().centerInside().into(readChapterImage, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
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
