package com.example.halftough.webcomreader;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.example.halftough.webcomreader.activities.ChapterList.ChapterPreferencesFragment;
import com.example.halftough.webcomreader.database.AppDatabase;
import com.example.halftough.webcomreader.database.Chapter;
import com.example.halftough.webcomreader.database.ChaptersDAO;
import com.example.halftough.webcomreader.database.ChaptersRepository;
import com.example.halftough.webcomreader.webcoms.Webcom;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Subclass for checking number of chapters, updating lists of chapters and downloading webcomics
 */
public class DownloaderService extends Service implements ChapterUpdateBroadcaster {
    private static final String ACTION_AUTODOWNLOAD = "ACTION_AUTODOWNLOAD";
    private static final String ACTION_AUTOREMOVE = "ACTION_AUTOREMOVE";
    private static final String ACTION_ENQUEUE_CHAPTER = "ACTION_ENQUEUE_CHAPTER";
    private static final String CHANNEL_DOWNLOADING = "CHANNEL_DOWNLOADING";
    private static final int FOREGROUND_ID = 6;

    ChapterDownloader downloader;
    private ChaptersDAO chaptersDAO;
    boolean serviceStarted = false;

    @Override
    public void onCreate() {
        downloader = new ChapterDownloader();
        AppDatabase db = AppDatabase.getDatabase(this);
        chaptersDAO = db.chaptersDAO();
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static void autodownload(Context context, String wid) {
        Intent intent = new Intent(context, DownloaderService.class);
        intent.setAction(ACTION_AUTODOWNLOAD);
        intent.putExtra(UserRepository.EXTRA_WEBCOM_ID, wid);
        context.startService(intent);
    }

    public static void autoremove(Context context, String wid) {
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
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null){
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
        return super.onStartCommand(intent, flags, startId);
    }

    private void handleEnqueueChapter(final String wid, final String chapter) {
        Webcom webcom = UserRepository.getWebcomInstance(wid);
        webcom.setChaptersDAO(chaptersDAO);
        final LiveData<String> url = webcom.getChapterUrl(chapter);
        url.observeForever(new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                url.removeObserver(this);
                if (s!=null && !s.isEmpty()) {
                    downloader.enqueue(s, new Chapter(wid, chapter));
                    updateNotification();
                }
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

    private void updateNotification(){
        Notification notification = buildNotification();

        if(!serviceStarted){
            startForeground(FOREGROUND_ID, notification);
            serviceStarted = true;
        }
        else{
            NotificationManager nManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            nManager.notify(FOREGROUND_ID, notification);
        }
    }

    private Notification buildNotification(){
        TreeMap<String, ChapterDownloadStatus> downloadStatus = downloader.status();

        String notificationTitle;
        String notificationText;
        Bitmap icon;

        if(downloadStatus.size() == 1){
            ChapterDownloadStatus status = downloadStatus.firstEntry().getValue();
            Webcom webcom = UserRepository.getWebcomInstance(status.getWid());
            notificationTitle = String.format(getString(R.string.download_service_downloading), webcom.getTitle());
            icon = BitmapFactory.decodeResource(getResources(), webcom.getIcon());
            if(status.isFinished()){
                notificationText = String.format(getString(R.string.download_service_downloading_content_finished), status.getAll());
            }
            else{
                notificationText = String.format(getString(R.string.download_service_downloading_content), status.getProgress(), status.getAll());
            }

        }
        else {
            notificationTitle = getString(R.string.download_service_downloading_few);
            icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
            StringBuilder str = new StringBuilder();
            for (TreeMap.Entry<String,ChapterDownloadStatus> entry : downloadStatus.entrySet()) {
                ChapterDownloadStatus status = entry.getValue();
                Webcom webcom = UserRepository.getWebcomInstance(status.getWid());
                if(str.length() > 0){
                    str.append("\n");
                }
                if(status.isFinished()) {
                    str.append( String.format(getString(R.string.download_service_downloading_content_few_finished), webcom.getTitle(), status.getAll()) );
                }
                else {
                    str.append(String.format(getString(R.string.download_service_downloading_content_few), webcom.getTitle(), status.getProgress(), status.getAll()));
                }
            }
            notificationText = str.toString();
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(DownloaderService.this, CHANNEL_DOWNLOADING)
                .setSmallIcon(R.drawable.ic_file_download_white_24dp)
                .setContentTitle(notificationTitle)
                .setContentText(notificationText)
                .setLargeIcon(icon)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(notificationText))
                .setPriority(NotificationCompat.PRIORITY_LOW);
        return mBuilder.build();
    }

    class ChapterDownloadStatus{
        private String wid;
        private int progress = 0;
        private int all = 0;
        private int finished = 0;

        public ChapterDownloadStatus(String wid){
            this.wid = wid;
        }

        public void enqueue(){
            all += 1;
        }
        public void startDownload() { progress += 1; }
        public void finishDownload() { finished += 1; }

        public String getWid() {
            return wid;
        }
        public int getProgress() {
            return progress;
        }
        public int getAll() {
            return all;
        }
        public boolean isFinished(){ return all==finished; }
    }

    class ChapterDownloader extends OneByOneUrlDownloader<Chapter> {
        TreeMap<String, ChapterDownloadStatus> statusMap = new TreeMap<>();

        @Override
        void onResponse(BufferedInputStream bufferInStream, Chapter extra, String extentsion) {
            saveBufferToFile(bufferInStream, extra, extentsion);
        }
        void onFail(Chapter chapter, String extentsion){
            chaptersDAO.setDownloadStatus(chapter.getWid(), chapter.getChapter(), Chapter.DownloadStatus.UNDOWNLOADED);
            broadcastChapterUpdated(chapter);
        }

        @Override
        public void enqueue(String element, Chapter chapter) {
            ChapterDownloadStatus status = statusMap.get(chapter.getWid());
            if(status == null){
                status = new ChapterDownloadStatus(chapter.getWid());
                statusMap.put(chapter.getWid(), status);
            }
            status.enqueue();
            super.enqueue(element, chapter);
        }

        @Override
        protected void downloadElement(String element, Chapter chapter) {
            ChapterDownloadStatus status = statusMap.get(chapter.getWid());
            status.startDownload();
            updateNotification();
            super.downloadElement(element, chapter);
        }

        @Override
        protected void elementDownloaded(Chapter finishedExtra) {
            ChapterDownloadStatus status = statusMap.get(finishedExtra.getWid());
            status.finishDownload();
            updateNotification();
            super.elementDownloaded(finishedExtra);
        }

        @Override
        protected void onFinished() {
            SharedPreferences gp = getSharedPreferences(UserRepository.GLOBAL_PREFERENCES, MODE_PRIVATE);
            boolean notify = gp.getBoolean("notify", true);
            if(notify){
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(DownloaderService.this);
                Notification notification = buildNotification();
                notificationManager.notify(UserRepository.nextNotificationID(), notification);
            }
            DownloaderService.this.stopForeground(true);
            DownloaderService.this.stopSelf();
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

        public TreeMap<String, ChapterDownloadStatus> status(){
            return statusMap;
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
