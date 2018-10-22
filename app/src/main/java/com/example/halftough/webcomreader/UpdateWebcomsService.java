package com.example.halftough.webcomreader;

import android.app.IntentService;
import android.app.Notification;
import android.app.Service;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.example.halftough.webcomreader.database.AppDatabase;
import com.example.halftough.webcomreader.database.Chapter;
import com.example.halftough.webcomreader.database.ChaptersDAO;
import com.example.halftough.webcomreader.database.ReadWebcom;
import com.example.halftough.webcomreader.database.ReadWebcomsDAO;
import com.example.halftough.webcomreader.webcoms.Webcom;

import java.util.Iterator;
import java.util.List;


public class UpdateWebcomsService extends Service implements ChapterUpdateBroadcaster {
    private static final String ACTION_UPDATE_NEW_CHAPTERS = "UPDATE_NEW_CHAPTERS";
    private static final String ACTION_UPDATE_NEW_CHAPTERS_IN = "UPDATE_NEW_CHAPTERS_IN";

    private static final String CHANNEL_UPDATE_NEW_CHAPTERS = "CHANNEL_UPDATE_NEW_CHAPTERS";

    private static final int FOREGROUND_ID = 7;

    private ReadWebcomsDAO readWebcomsDAO;
    private ChaptersDAO chaptersDAO;
    private boolean isLoop;


    @Override
    public void onCreate() {
        AppDatabase db = AppDatabase.getDatabase(this);
        readWebcomsDAO = db.readWebcomsDAO();
        chaptersDAO = db.chaptersDAO();
        isLoop = false;
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    public static void updateNewChapters(Context context) {
        Intent intent = new Intent(context, UpdateWebcomsService.class);
        intent.setAction(ACTION_UPDATE_NEW_CHAPTERS);
        context.startService(intent);
    }

    public static void updateNewChaptersIn(Context context, String wid){
        Intent intent = new Intent(context, DownloaderService.class);
        intent.setAction(ACTION_UPDATE_NEW_CHAPTERS_IN);
        intent.putExtra(UserRepository.EXTRA_WEBCOM_ID, wid);
        context.startService(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null && intent.getAction()!=null){
            switch (intent.getAction()){
                case ACTION_UPDATE_NEW_CHAPTERS:
                    handleUpdateNewChapters();
                    break;
                case ACTION_UPDATE_NEW_CHAPTERS_IN:
                    String wid = intent.getStringExtra(UserRepository.EXTRA_WEBCOM_ID);
                    handleUpdateNewChaptersIn(wid, null);
                    break;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void handleUpdateNewChapters() {
        isLoop = true;
        final LiveData<List<ReadWebcom>> webcoms =  readWebcomsDAO.getAll();
        webcoms.observeForever( new Observer<List<ReadWebcom>>() {
            @Override
            public void onChanged(@Nullable List<ReadWebcom> readWebcoms) {
                webcoms.removeObserver(this);
                Iterator<ReadWebcom> it = readWebcoms.iterator();
                updateRec(it);
            }
        });
    }

    private void updateRec(final Iterator<ReadWebcom> it){
        if(it.hasNext()){
            final ReadWebcom webcom = it.next();
            handleUpdateNewChaptersIn(webcom.getWid(), new TaskDelegate(){
                @Override
                public void onFinish() {
                    DownloaderService.autodownload(UpdateWebcomsService.this, webcom.getWid());
                    updateRec(it);
                }
            });
        }
        else{
            isLoop = false;
            stopSelf();
        }
    }

    private void handleUpdateNewChaptersIn(String wid, final TaskDelegate delegate) {
        Webcom webcom = UserRepository.getWebcomInstance(wid);
        Notification notification;
        Bitmap icon = BitmapFactory.decodeResource(getResources(), webcom.getIcon());
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(UpdateWebcomsService.this, CHANNEL_UPDATE_NEW_CHAPTERS)
                .setSmallIcon(R.drawable.ic_refresh_white_24dp)
                .setContentTitle( String.format(getString(R.string.update_service_updating), webcom.getTitle()) )
                .setLargeIcon(icon)
                .setPriority(NotificationCompat.PRIORITY_LOW);
        notification = mBuilder.build();

        startForeground(FOREGROUND_ID, notification);

        Thread t = new Thread(new UpdateWebcomRunnable(webcom, new TaskDelegate(){
            @Override
            public void onFinish() {
                stopForeground(true);
                if(!isLoop) {
                    stopSelf();
                }
                if(delegate != null) {
                    delegate.onFinish();
                }
            }
        }));
        t.start();
    }

    class UpdateWebcomRunnable implements Runnable{
        Webcom webcom;
        TaskDelegate delegate;
        public UpdateWebcomRunnable(Webcom webcom, TaskDelegate delegate){
            this.webcom = webcom;
            this.delegate = delegate;
        }
        @Override
        public void run() {
            webcom.updateChapterList(UpdateWebcomsService.this, chaptersDAO, readWebcomsDAO, new TaskDelegate(){
                @Override
                public void onFinish() {
                    delegate.onFinish();
                }
            });
        }
    }

    //TODO duplicated code with DownloaderService
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
