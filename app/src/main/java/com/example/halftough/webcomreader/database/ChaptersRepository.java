package com.example.halftough.webcomreader.database;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.os.AsyncTask;
import android.support.annotation.Nullable;

import com.example.halftough.webcomreader.NoWebcomClassException;
import com.example.halftough.webcomreader.OneByOneDownloader;
import com.example.halftough.webcomreader.UserRepository;
import com.example.halftough.webcomreader.webcoms.ComicPage;
import com.example.halftough.webcomreader.webcoms.Webcom;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Semaphore;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChaptersRepository {
    private ChaptersDAO chaptersDAO;
    private MutableLiveData<List<Chapter>> chapters;
    private Semaphore chaptersSemaphore;
    private LiveData<Integer> chapterCount;
    private Webcom webcom;

    public ChaptersRepository(Application application, final String wid){
        AppDatabase db = AppDatabase.getDatabase(application);
        chaptersDAO = db.chaptersDAO();
        chapters = new MutableLiveData<>();
        chaptersSemaphore = new Semaphore(1);
        try {
            webcom = UserRepository.getWebcomInstance(wid);

            //We don't know if it was updated yet
            chapterCount = webcom.getChapterCount();
            chapterCount.observeForever(new Observer<Integer>() {
                @Override
                public void onChanged(@Nullable Integer integer) {
                    dataLoaded();
                    chapterCount.removeObserver(this);
                }
            });
            chapters.observeForever(new Observer<List<Chapter>>() {
                @Override
                public void onChanged(@Nullable List<Chapter> chapters2) {
                    dataLoaded();
                    chapters.removeObserver(this);
                }
            });
            // Get chapters from database.
            // We have two variables one for chapters in general, one for the ones got from database,
            // as LiveData can't be casted into MutableLiveData and we need mutable if we want to add
            // new elements as we download them later
            LiveData<List<Chapter>> dbChapters = chaptersDAO.getChapters(webcom.getId());
            dbChapters.observeForever(new Observer<List<Chapter>>() {
                @Override
                public void onChanged(@Nullable List<Chapter> dbChapters) {
                    chapters.postValue(dbChapters);
                }
            });
        } catch (NoWebcomClassException e) {
            e.printStackTrace();
        }
    }

    private void dataLoaded(){
        final List<Chapter> chapterList = chapters.getValue();
        Integer chapterInt = chapterCount.getValue();
        if(chapterList == null || chapterInt == null )
            return;
        // see if chapter count matches what we have in database
        // if not, download data and put it in the database
        if(chapterList.size() < chapterInt){
            int size = chapterList.size();
            Set<String> stringChapters = new TreeSet<>();
            for(Chapter chapter : chapterList){
                stringChapters.add(chapter.getChapter());
            }

            List<Call<ComicPage>> calls = new ArrayList<>();
            List<String> allChapters = webcom.getChapterList();
            for(String chap : allChapters){
                if( !stringChapters.contains(chap)){
                    calls.add( webcom.getPageCall(chap) );
                }
            }
            new OneByOneDownloader<ComicPage>(calls, 5){
                @Override
                public void onMyResponse(Call<ComicPage> call, Response<ComicPage> response) {
                    if(response.body() != null) {
                        Chapter chapter = new Chapter(webcom.getId(), response.body().getNum());
                        chapter.setTitle(response.body().getTitle());
                        insertChapter(chapter);
                        chaptersSemaphore.acquireUninterruptibly();
                        chapterList.add(findIndexFor(chapter.getChapter()), chapter);
                        chapters.postValue(chapterList);
                        chaptersSemaphore.release();
                    }
                }
            }.download();

        }
    }

    private int findIndexFor(String chapter) {
        List<Chapter> chapterList = chapters.getValue();
        Float chap = Float.parseFloat(chapter);
        int index = chapterList.size()/2;
        int low = 0;
        int top = chapterList.size();
        if( top == 0 ){
            return 0;
        }
        if( chap > Float.parseFloat( chapterList.get(top-1).getChapter() )){
            return top;
        }

        while(true){
            float onIndex = Float.parseFloat( chapterList.get(index-1).getChapter() );
            if( low == index){
                return index;
            }
            else if( chap < onIndex ){
                top = index;
                index = (low+top)/2;
            }
            else if( chap > onIndex){
                low = index;
                index = (low+top)/2;
            }
        }
    }

    public LiveData<List<Chapter>> getChapters(){
        return chapters;
    }

    public void insertChapter(Chapter chapter){
        new insertAsyncTask(chaptersDAO).execute(chapter);
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
}
