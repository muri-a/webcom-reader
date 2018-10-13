package com.example.halftough.webcomreader;

import android.app.IntentService;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import com.example.halftough.webcomreader.activities.ChapterList.ChapterPreferencesFragment;
import com.example.halftough.webcomreader.database.AppDatabase;
import com.example.halftough.webcomreader.database.Chapter;
import com.example.halftough.webcomreader.database.ChaptersDAO;
import com.example.halftough.webcomreader.database.ChaptersRepository;
import com.example.halftough.webcomreader.database.ReadWebcom;
import com.example.halftough.webcomreader.database.ReadWebcomsDAO;
import com.example.halftough.webcomreader.webcoms.Webcom;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Subclass for checking number of chapters, updating lists of chapters and downloading webcomics
 */
public class DownloaderService extends IntentService {
    private static final String ACTION_UPDATE_NEW_CHAPTERS = "UPDATE_NEW_CHAPTERS";
    private static final String ACTION_UPDATE_NEW_CHAPTERS_IN = "UPDATE_NEW_CHAPTERS_IN";
    private static final String ACTION_AUTODOWNLOAD = "ACTION_AUTODOWNLOAD";
    private static final String ACTION_ENQUEUE_CHAPTER = "ACTION_ENQUEUE_CHAPTER";

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

    public static void autodownload(Context context, String wid){
        Intent intent = new Intent(context, DownloaderService.class);
        intent.setAction(ACTION_AUTODOWNLOAD);
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
            switch(action){
                case ACTION_UPDATE_NEW_CHAPTERS:
                    handleUpdateNewChapters();
                    break;
                case ACTION_UPDATE_NEW_CHAPTERS_IN:
                    handleUpdateNewChaptersIn(intent.getStringExtra(UserRepository.EXTRA_WEBCOM_ID));
                    break;
                case ACTION_AUTODOWNLOAD: {
                    String wid = intent.getStringExtra(UserRepository.EXTRA_WEBCOM_ID);
                    handleAutodownload(wid);
                    break;
                }
                case ACTION_ENQUEUE_CHAPTER: {
                    final String wid = intent.getStringExtra(UserRepository.EXTRA_WEBCOM_ID);
                    final String chapter = intent.getStringExtra(UserRepository.EXTRA_CHAPTER_NUMBER);
                    handleEnqueueChapter(wid, chapter);
                    break;
                }
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
                    //TODO only autodownload after update is finished
                    handleAutodownload(webcom.getWid());
                }
            }
        });
    }

    private void handleUpdateNewChaptersIn(String wid) {
        final Webcom webcom = UserRepository.getWebcomInstance(wid);
        webcom.updateChapterList(this, chaptersDAO, readWebcomsDAO);
    }

    private void handleEnqueueChapter(final String wid, final String chapter) {
        Webcom webcom = UserRepository.getWebcomInstance(wid);
        final LiveData<String> url = webcom.getChapterUrl(chapter);
        url.observeForever(new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
            url.removeObserver(this);
            if(!s.isEmpty())
                downloader.enqueue(s, new Chapter(wid, chapter));
            }
        });
    }

    private void handleAutodownload(String wid){
        Webcom webcom = UserRepository.getWebcomInstance(wid);
        SharedPreferences chapterPreferences = getSharedPreferences(ChapterPreferencesFragment.PREFERENCE_KEY_COMIC+wid, MODE_PRIVATE);
        SharedPreferences globalPreferences = getSharedPreferences(UserRepository.GLOBAL_PREFERENCES, MODE_PRIVATE);
        PreferenceHelper.AutodownloadSetting mode = PreferenceHelper.getAutodownloadSetting(this, chapterPreferences, globalPreferences, webcom);
        if(mode == PreferenceHelper.AutodownloadSetting.NONE){
            return;
        }

        int autodownloadNumber = PreferenceHelper.getAutodownloadnumber(this, chapterPreferences, globalPreferences);

        final LiveData<List<Chapter>> chapters;
        if(mode == PreferenceHelper.AutodownloadSetting.NEWEST){
            chapters = chaptersDAO.getNewestUnread(wid, autodownloadNumber);
        }
        else{
            chapters = chaptersDAO.getOldestUnread(wid, autodownloadNumber);
        }
        chapters.observeForever( new Observer<List<Chapter>>() {
            @Override
            public void onChanged(@Nullable List<Chapter> changed) {
                chapters.removeObserver(this);
                for(Chapter chapter : changed){
                    if(chapter.getDownloadStatus() == Chapter.DownloadStatus.UNDOWNLOADED){
                        ChaptersRepository.setDownloadStatus(chapter, Chapter.DownloadStatus.DOWNLOADING, chaptersDAO);
                        //TODO wait for setDownloadStatus to finish
                        handleEnqueueChapter(chapter.getWid(), chapter.getChapter());
                    }
                }
            }
        });
    }

    class ChapterDownloader extends OneByOneUrlDownloader<Chapter> {
        @Override
        void onResponse(BufferedInputStream bufferInStream, Chapter extra, String extentsion) {
            saveBufferToFile(bufferInStream, extra, extentsion);
        }
        void onFail(Chapter chapter, String extentsion){
            chaptersDAO.setDownloadStatus(chapter.getWid(), chapter.getChapter(), Chapter.DownloadStatus.UNDOWNLOADED);
            broadcastChapterUpdated(chapter);
        }

        private void saveBufferToFile(BufferedInputStream bufferedInputStream, Chapter chapter, String extension){
            // TODO option to save internal or external
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

    public void broadcastChapterUpdated(Chapter chapter){
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(UserRepository.ACTION_CHAPTER_UPDATED);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(UserRepository.EXTRA_WEBCOM_ID, chapter.getWid());
        broadcastIntent.putExtra(UserRepository.EXTRA_CHAPTER_NUMBER, chapter.getChapter());
        sendBroadcast(broadcastIntent);
    }

}
