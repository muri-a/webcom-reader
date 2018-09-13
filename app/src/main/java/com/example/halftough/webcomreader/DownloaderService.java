package com.example.halftough.webcomreader;

import android.app.IntentService;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.halftough.webcomreader.activities.ChapterList.ChapterListActivity;
import com.example.halftough.webcomreader.activities.ChapterList.ChapterListReciever;
import com.example.halftough.webcomreader.activities.MyWebcoms.MyWebcomsActivity;
import com.example.halftough.webcomreader.database.AppDatabase;
import com.example.halftough.webcomreader.database.Chapter;
import com.example.halftough.webcomreader.database.ChaptersDAO;
import com.example.halftough.webcomreader.database.ReadWebcom;
import com.example.halftough.webcomreader.database.ReadWebcomsDAO;
import com.example.halftough.webcomreader.webcoms.ComicPage;
import com.example.halftough.webcomreader.webcoms.Webcom;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Subclass for checking number of chapters, updating lists of chapters and downloading webcomics
 */
public class DownloaderService extends IntentService {
    private static final String ACTION_UPDATE_NEW_CHAPTERS = "UPDATE_NEW_CHAPTERS";
    private static final String ACTION_ENQUEUE_CHAPTER = "ACTION_ENQUEUE_CHAPTER";
    private static final String ACTION_UPDATE_NEW_CHAPTERS_IN = "UPDATE_NEW_CHAPTERS_IN";

    OneByOneUrlDownloader downloader;
    private ChaptersDAO chaptersDAO;
    private ReadWebcomsDAO readWebcomsDAO;

    public DownloaderService() {
        super("DownloaderService");
        downloader = new ChapterDownloader();
        AppDatabase db = AppDatabase.getDatabase(this);
        chaptersDAO = db.chaptersDAO();
        readWebcomsDAO = db.readWebcomsDAO();
    }

    public static void updateNewChapters(Context context) {
        Intent intent = new Intent(context, DownloaderService.class);
        intent.setAction(ACTION_UPDATE_NEW_CHAPTERS);
        context.startService(intent);
    }

    public static void updateNewChaptersIn(Context context, String wid){
        Intent intent = new Intent(context, DownloaderService.class);
        intent.setAction(ACTION_UPDATE_NEW_CHAPTERS_IN);
        intent.putExtra(ChapterListActivity.CHAPTER_WID, wid);
        context.startService(intent);
    }

    public static void enqueueChapter(Context context, Chapter chapter) {
        Intent intent = new Intent(context, DownloaderService.class);
        intent.setAction(ACTION_ENQUEUE_CHAPTER);
        intent.putExtra(ChapterListActivity.CHAPTER_WID, chapter.getWid());
        intent.putExtra(ChapterListActivity.CHAPTER_NUMBER, chapter.getChapter());
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_UPDATE_NEW_CHAPTERS.equals(action)) {
                handleUpdateNewChapters();
            } else if(ACTION_UPDATE_NEW_CHAPTERS_IN.equals(action)){
                handleUpdateNewChaptersIn(intent.getStringExtra(ChapterListActivity.CHAPTER_WID));
            } else if (ACTION_ENQUEUE_CHAPTER.equals(action)) {
                final String wid = intent.getStringExtra(ChapterListActivity.CHAPTER_WID);
                final String chapter = intent.getStringExtra(ChapterListActivity.CHAPTER_NUMBER);
                handleEnqueueChapter(wid, chapter);
            }
        }
    }

    private void handleUpdateNewChapters() {
        final LiveData<List<ReadWebcom>> webcoms =  readWebcomsDAO.getAll();
        webcoms.observeForever( new Observer<List<ReadWebcom>>() {
            @Override
            public void onChanged(@Nullable List<ReadWebcom> readWebcoms) {
                webcoms.removeObserver(this);
                for(ReadWebcom webcom : readWebcoms){
                    handleUpdateNewChaptersIn(webcom.getWid());
                }
            }
        });
    }

    private void handleUpdateNewChaptersIn(String wid) {
        try {
            final Webcom webcom = UserRepository.getWebcomInstance(wid);
            //First we update chapter counter, because it might affect list of all chapters
            final LiveData<Integer> chapterCountNet = webcom.getChapterCount();
            final LiveData<List<Chapter>> dbChapters = chaptersDAO.getChapters(wid);
            new OnLiveDataReady(){
                @Override
                public void onReady() {
                    updateNewChaptersInReady(webcom, dbChapters);
                }
            }.observe(dbChapters, chapterCountNet, OnLiveDataReady.WaitUntil.CHANGED);
            webcom.updateChapters();
        } catch (NoWebcomClassException e) {
            e.printStackTrace();
        }
    }

    //TODO? LiveData<Chapters> to Chapters
    private void updateNewChaptersInReady(Webcom webcom, LiveData<List<Chapter>> dbChapters){
        List<String> netChapters = webcom.getChapterList();
        Queue<Call<ComicPage>> calls = new LinkedList<>();
        Queue<Chapter> extra = new LinkedList<>(); // References to chapters that will be downloaded

        Iterator<String> netIt = netChapters.iterator();
        Iterator<Chapter> dbIt = dbChapters.getValue().iterator();

        String netChapter = netIt.hasNext()?netIt.next():null;
        Chapter dbChapter = dbIt.hasNext()?dbIt.next():null;
        if(netChapter == null && dbChapter == null)
            return;
        do{
            //If there is chapter on list that isn't in our database
            if(dbChapter==null || (netChapter!=null && Float.parseFloat(netChapter) < Float.parseFloat(dbChapter.getChapter())) ){
                Chapter chapter = new Chapter(webcom.getId(), netChapter);
                calls.add(webcom.getChapterMetaCall(netChapter));
                extra.add(chapter);
                netChapter = netIt.hasNext()?netIt.next():null;
            }
            //If there is chapter in database that isn't on the list
            else if(netChapter==null || Float.parseFloat(netChapter) > Float.parseFloat(dbChapter.getChapter())){
                //TODO remove it from database if it haven't been downloaded
                dbChapter = dbIt.hasNext()?dbIt.next():null;
            }
            //If chapter is both in database and on the list
            else{
                netChapter = netIt.hasNext()?netIt.next():null;
                dbChapter = dbIt.hasNext()?dbIt.next():null;
            }
        }while(netIt.hasNext() || dbIt.hasNext());

        new OneByOneCallDownloader<ComicPage, Chapter>(calls, extra, 5){
            @Override
            public void onResponse(Call<ComicPage> call, Response<ComicPage> response, Chapter extra) {
                if(response.body() != null) {
                    //extra is reference to chapter in the list we use, so we can update it from here.
                    extra.setTitle(response.body().getTitle());
                    insertChapter(extra);
                }
            }
        }.download();
    }

    //TODO Duplicated code, wonder if I should move it somewhere
    public void insertChapter(Chapter chapter){
        new insertAsyncTask(chaptersDAO).execute(chapter);
    }

    private class insertAsyncTask extends AsyncTask<Chapter, Void, Void> {
        private ChaptersDAO mAsyncTaskDao;
        insertAsyncTask(ChaptersDAO dao) {
            mAsyncTaskDao = dao;
        }
        @Override
        protected Void doInBackground(final Chapter... params) {
            mAsyncTaskDao.insert(params[0]);
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(ChapterListReciever.ACTION_CHAPTER_UPDATED);
            broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
            broadcastIntent.putExtra(MyWebcomsActivity.WEBCOM_ID, params[0].getWid());
            sendBroadcast(broadcastIntent);
            return null;
        }
    }

    private void handleEnqueueChapter(final String wid, final String chapter) {
        try {
            Webcom webcom = UserRepository.getWebcomInstance(wid);
            final LiveData<String> url = webcom.getChapterUrl(chapter);
            url.observeForever(new Observer<String>() {
                @Override
                public void onChanged(@Nullable String s) {
                    url.removeObserver(this);
                    downloader.enqueue(s, new Chapter(wid, chapter));
                }
            });
        } catch (NoWebcomClassException e) {
            e.printStackTrace();
        }
    }

    class ChapterDownloader extends OneByOneUrlDownloader<Chapter> {
        @Override
        void onResponse(BufferedInputStream bufferInStream, Chapter extra, String extentsion) {
            // TODO option to save internal or external
            saveBufferToFile(bufferInStream, extra, extentsion);
        }

        private void saveBufferToFile(BufferedInputStream bufferedInputStream, Chapter chapter, String extension){
            File root = android.os.Environment.getExternalStorageDirectory();
            File dir = new File(root.getAbsolutePath()+"/webcom/"+chapter.getWid());
            if(!dir.exists()){
                dir.mkdirs();
            }
            File file = new File(dir, chapter.getChapter()+extension);
            if(file.exists()){
                file.delete();
            }
            try {
                file.createNewFile();

                FileOutputStream fos = new FileOutputStream(file);
                int cur;
                while( (cur = bufferedInputStream.read()) != -1){
                    fos.write(cur);
                }
                fos.flush();
                fos.close();
                chaptersDAO.setDownloadStatus(chapter.getWid(), chapter.getChapter(), Chapter.DownloadStatus.DOWNLOADED);
            } catch (FileNotFoundException e) {
                chaptersDAO.setDownloadStatus(chapter.getWid(), chapter.getChapter(), Chapter.DownloadStatus.UNDOWNLOADED);
                e.printStackTrace();
            } catch (IOException e) {
                chaptersDAO.setDownloadStatus(chapter.getWid(), chapter.getChapter(), Chapter.DownloadStatus.UNDOWNLOADED);
                e.printStackTrace();
            }
        }
    }
}
