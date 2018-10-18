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

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Subclass for checking number of chapters, updating lists of chapters and downloading webcomics
 */
public class DownloaderService extends IntentService implements ChapterUpdateBroadcaster {
    private static final String ACTION_AUTODOWNLOAD = "ACTION_AUTODOWNLOAD";
    private static final String ACTION_AUTOREMOVE = "ACTION_AUTOREMOVE";
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

    public static void autodownload(Context context, String wid){
        Intent intent = new Intent(context, DownloaderService.class);
        intent.setAction(ACTION_AUTODOWNLOAD);
        intent.putExtra(UserRepository.EXTRA_WEBCOM_ID, wid);
        context.startService(intent);
    }

    public static void autoremove(Context context, String wid){
        Intent intent = new Intent(context, DownloaderService.class);
        intent.setAction(ACTION_AUTOREMOVE);
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
                case ACTION_AUTODOWNLOAD: {
                    String wid = intent.getStringExtra(UserRepository.EXTRA_WEBCOM_ID);
                    handleAutodownload(wid);
                    break;
                }
                case ACTION_AUTOREMOVE: {
                    String wid = intent.getStringExtra(UserRepository.EXTRA_WEBCOM_ID);
                    handleAutoremove(wid);
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

    private void handleAutoremove(String wid){
        //If autoreme is enabled for this webcom
        if( PreferenceHelper.getAutoremove(this, wid) ) {
            final LiveData<List<Chapter>> readChapters = chaptersDAO.getChapters(wid, Chapter.Status.READ, Chapter.DownloadStatus.DOWNLOADED);
            SharedPreferences preferences = getSharedPreferences(ChapterPreferencesFragment.PREFERENCE_KEY_COMIC+wid, MODE_PRIVATE);
            final Set<String> last = new TreeSet<>();
            try {
                JSONArray jarr = new JSONArray(preferences.getString("last_list", "[]"));
                for(int i=0; i<jarr.length(); i++){
                    last.add(jarr.getString(i));
                }
            } catch (JSONException e) {
            }
            readChapters.observeForever(new Observer<List<Chapter>>() {
                @Override
                public void onChanged(@Nullable List<Chapter> chapters) {
                    if(chapters != null) {
                        readChapters.removeObserver(this);
                        for(Chapter chapter : chapters){
                            if(!last.contains(chapter.getChapter())){
                                UserRepository.deleteChapter(chapter);
                                ChaptersRepository.setDownloadStatus(chapter, Chapter.DownloadStatus.UNDOWNLOADED, chaptersDAO);
                                broadcastChapterUpdated(chapter);
                            }
                        }
                    }
                }
            });
        }
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

    @Override
    public void broadcastChapterUpdated(Chapter chapter){
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(UserRepository.ACTION_CHAPTER_UPDATED);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(UserRepository.EXTRA_WEBCOM_ID, chapter.getWid());
        broadcastIntent.putExtra(UserRepository.EXTRA_CHAPTER_NUMBER, chapter.getChapter());
        sendBroadcast(broadcastIntent);
    }

}
