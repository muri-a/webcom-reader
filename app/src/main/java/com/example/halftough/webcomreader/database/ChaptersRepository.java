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
    private LiveData<Integer> chapterCount;
    private Webcom webcom;
    private Application application;

    public ChaptersRepository(Application application, final String wid){
        this.application = application;
        AppDatabase db = AppDatabase.getDatabase(application);
        chaptersDAO = db.chaptersDAO();
        chapters = new MutableLiveData<>();
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
        Integer chapterInt = chapterCount.getValue();
        if(chapters.getValue() == null || chapterInt == null )
            return;

        List<String> allChapters = webcom.getChapterList();
        List<Chapter> dbChapters = chapters.getValue();

        final List<Chapter> chapterList = new ArrayList<>();
        Queue<Call<ComicPage>> calls = new LinkedList<>();
        Queue<Chapter> extra = new LinkedList<>(); // References to chapters that will be downloaded

        Iterator<String> allIt = allChapters.iterator();
        Iterator<Chapter> dbIt = dbChapters.iterator();

        // TODO Zdecydować co jeśli wpis istnieje w bazie, ale nie danych ze strony. Na razie jest po prostu dodawany do listy.
        // Jeśli komiks nie jest pobrany, powinna być usuwana z bazy
        String a = null;
        Chapter b = null;
        if(allIt.hasNext())
            a = allIt.next();
        if(dbIt.hasNext())
            b = dbIt.next();
        // TODO do while instead (?)
        while(allIt.hasNext() || dbIt.hasNext()){
            if(b==null || (a!=null && Float.parseFloat(a) < Float.parseFloat(b.getChapter())) ){
                Chapter chapter = new Chapter(webcom.getId(), a);
                chapterList.add(chapter);
                calls.add(webcom.getChapterMetaCall(a));
                extra.add(chapter);
                a = allIt.hasNext()?allIt.next():null;
            }
            else if(a==null || Float.parseFloat(a) > Float.parseFloat(b.getChapter())){
                chapterList.add(b);
                b = dbIt.hasNext()?dbIt.next():null;
            }
            else{
                chapterList.add(b);
                a = allIt.hasNext()?allIt.next():null;
                b = dbIt.hasNext()?dbIt.next():null;
            }
        }
        chapters.postValue(chapterList);

        new OneByOneCallDownloader<ComicPage, Chapter>(calls, extra, 5){
            @Override
            public void onResponse(Call<ComicPage> call, Response<ComicPage> response, Chapter extra) {
                if(response.body() != null) {
                    //extra is reference to chapter in the list we use, so we can update it from here.
                    extra.setTitle(response.body().getTitle());
                    insertChapter(extra);
                    chapters.postValue(chapterList);
                }
            }
        }.download();
    }

    public MutableLiveData<List<Chapter>> getChapters(){
        return chapters;
    }

    public void insertChapter(Chapter chapter){
        new insertAsyncTask(chaptersDAO).execute(chapter);
    }

    public void downloadChapter(Chapter chapter){
        chapter.setDownloadStatus(Chapter.DownloadStatus.DOWNLOADING);
        new setDownloadStatusAsyncTask(chaptersDAO).execute(chapter);
        DownloaderService.enqueueChapter(application, chapter);
    }

    public void update() {
        final LiveData<List<Chapter>> dbChapters = chaptersDAO.getChapters(webcom.getId());
        dbChapters.observeForever(new Observer<List<Chapter>>() {
            @Override
            public void onChanged(@Nullable List<Chapter> chaps) {
                dbChapters.removeObserver(this);
                Iterator<Chapter> chIt = chaps.iterator();
                Iterator<Chapter> dbIt = dbChapters.getValue().iterator();
                List<Chapter> toAdd = new ArrayList<>();
                Chapter a = chIt.hasNext()?chIt.next():null;
                Chapter b = dbIt.hasNext()?dbIt.next():null;
                do{
                    if(a.equals(b)){
                        if(a.getStatus() != b.getStatus()){
                            a.setStatus(b.getStatus());
                        }
                        a = chIt.hasNext()?chIt.next():null;
                        b = dbIt.hasNext()?dbIt.next():null;
                    }
                    else if( a.compareTo(b) < 0 ){
                        a = chIt.hasNext()?chIt.next():null;
                    }
                    else{
                        // Because this should be rare, we don't care for speed
                        toAdd.add(b);
                        b = dbIt.hasNext()?dbIt.next():null;
                    }
                }while(chIt.hasNext() || dbIt.hasNext());
                if(toAdd.size() > 0){
                    Set<Chapter> set = new TreeSet<>(chaps);
                    for(Chapter c : toAdd){
                        set.add(c);
                    }
                    chaps = new ArrayList<>(set);
                }
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

    private static class updateListAsyncTask extends AsyncTask<List<Chapter>, Void, Void>{
        private ChaptersDAO mAsyncTaskDao;
        updateListAsyncTask(ChaptersDAO dao){ mAsyncTaskDao = dao; }
        @Override
        protected Void doInBackground(List<Chapter>... lists) {
            mAsyncTaskDao.update(lists[0]);
            return null;
        }
    }

    private static class setDownloadStatusAsyncTask extends AsyncTask<Chapter, Void, Void>{
        private ChaptersDAO mAsyncTaskDao;
        public setDownloadStatusAsyncTask(ChaptersDAO dao) {
            mAsyncTaskDao = dao;
        }
        @Override
        protected Void doInBackground(Chapter... chapters) {
            mAsyncTaskDao.setDownloadStatus(chapters[0].getWid(), chapters[0].getChapter(), chapters[0].getDownloadStatus());
            return null;
        }
    }
}
