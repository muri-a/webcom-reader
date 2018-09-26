package com.example.halftough.webcomreader;

import android.app.IntentService;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.halftough.webcomreader.database.AppDatabase;
import com.example.halftough.webcomreader.database.Chapter;
import com.example.halftough.webcomreader.database.ChaptersDAO;
import com.example.halftough.webcomreader.database.ReadWebcom;
import com.example.halftough.webcomreader.database.ReadWebcomsDAO;
import com.example.halftough.webcomreader.webcoms.ComicPage;
import com.example.halftough.webcomreader.webcoms.Webcom;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
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
        intent.putExtra(UserRepository.EXTRA_WEBCOM_ID, wid);
        context.startService(intent);
    }

    public static void enqueueChapter(Context context, Chapter chapter) {
        Intent intent = new Intent(context, DownloaderService.class);
        intent.setAction(ACTION_ENQUEUE_CHAPTER);
        intent.putExtra(UserRepository.EXTRA_WEBCOM_ID, chapter.getWid());
        intent.putExtra(UserRepository.EXTRA_CHAPTER_NUMBER, chapter.getChapter());
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_UPDATE_NEW_CHAPTERS.equals(action)) {
                handleUpdateNewChapters();
            } else if(ACTION_UPDATE_NEW_CHAPTERS_IN.equals(action)){
                handleUpdateNewChaptersIn(intent.getStringExtra(UserRepository.EXTRA_WEBCOM_ID));
            } else if (ACTION_ENQUEUE_CHAPTER.equals(action)) {
                final String wid = intent.getStringExtra(UserRepository.EXTRA_WEBCOM_ID);
                final String chapter = intent.getStringExtra(UserRepository.EXTRA_CHAPTER_NUMBER);
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
        final Webcom webcom = UserRepository.getWebcomInstance(wid);
        //First we update chapter counter, because it might affect list of all chapters
        final LiveData<Integer> chapterCountNet = webcom.getChapterCount();
        final LiveData<List<Chapter>> dbChapters = chaptersDAO.getChapters(wid);
        new OnLiveDataReady(){
            @Override
            public void onReady() {
                updateNewChaptersInReady(webcom, dbChapters.getValue());
            }
        }.observe(dbChapters, chapterCountNet, OnLiveDataReady.WaitUntil.CHANGED);
        webcom.updateChapters();
    }

    private void updateNewChaptersInReady(Webcom webcom, List<Chapter> dbChapters){
        List<String> netChapters = webcom.getChapterList();
        Queue<Call<ComicPage>> calls = new LinkedList<>();
        Queue<Chapter> extra = new LinkedList<>(); // References to chapters that will be downloaded

        if(netChapters == null || dbChapters == null)
            return;

        Iterator<String> netIt = netChapters.iterator();
        Iterator<Chapter> dbIt = dbChapters.iterator();

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
        }while(netChapter != null || dbChapter != null);

        updateWebcomCount(webcom.getId(), dbChapters.size()+calls.size());

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

    private void updateWebcomCount(String wid, int count) {
        ReadWebcom webcom = new ReadWebcom(wid);
        webcom.setChapterCount(count);
        new updateReadWebcomAsyncTask(readWebcomsDAO).execute(webcom);
    }

    public void insertChapter(Chapter chapter){
        new insertAsyncTask(chaptersDAO, this).execute(chapter);
    }

    private static class insertAsyncTask extends AsyncTask<Chapter, Void, Void> {
        private ChaptersDAO mAsyncTaskDao;
        WeakReference<DownloaderService> dService;
        insertAsyncTask(ChaptersDAO dao, DownloaderService downloaderService) {
            mAsyncTaskDao = dao;
            dService = new WeakReference<>(downloaderService);
        }
        @Override
        protected Void doInBackground(final Chapter... params) {
            mAsyncTaskDao.insert(params[0]);
            dService.get().broadcastChapterUpdated(params[0]);
            return null;
        }
    }

    private static class updateReadWebcomAsyncTask extends AsyncTask<ReadWebcom, Void, Void> {
        private ReadWebcomsDAO mAsyncTaskDao;
        updateReadWebcomAsyncTask(ReadWebcomsDAO dao){
            mAsyncTaskDao = dao;
        }
        @Override
        protected Void doInBackground(ReadWebcom... readWebcoms) {
            mAsyncTaskDao.updateChapterCount(readWebcoms[0].getWid(), readWebcoms[0].getChapterCount());
            return null;
        }
    }

    private void handleEnqueueChapter(final String wid, final String chapter) {
        Webcom webcom = UserRepository.getWebcomInstance(wid);
        final LiveData<String> url = webcom.getChapterUrl(chapter);
        url.observeForever(new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
            url.removeObserver(this);
            downloader.enqueue(s, new Chapter(wid, chapter));
            }
        });
    }

    class ChapterDownloader extends OneByOneUrlDownloader<Chapter> {
        @Override
        void onResponse(BufferedInputStream bufferInStream, Chapter extra, String extentsion) {
            // TODO option to save internal or external
            saveBufferToFile(bufferInStream, extra, extentsion);
        }
        void onFail(Chapter chapter, String extentsion){
            chaptersDAO.setDownloadStatus(chapter.getWid(), chapter.getChapter(), Chapter.DownloadStatus.UNDOWNLOADED);
            broadcastChapterUpdated(chapter);
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
            } catch (IOException e) {
                chaptersDAO.setDownloadStatus(chapter.getWid(), chapter.getChapter(), Chapter.DownloadStatus.UNDOWNLOADED);
            }
            finally {
                broadcastChapterUpdated(chapter);
            }
        }
    }

    private void broadcastChapterUpdated(Chapter chapter){
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(UserRepository.ACTION_CHAPTER_UPDATED);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(UserRepository.EXTRA_WEBCOM_ID, chapter.getWid());
        broadcastIntent.putExtra(UserRepository.EXTRA_CHAPTER_NUMBER, chapter.getChapter());
        sendBroadcast(broadcastIntent);
    }
}
