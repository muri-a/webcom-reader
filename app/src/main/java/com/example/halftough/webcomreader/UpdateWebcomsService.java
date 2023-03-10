package com.example.halftough.webcomreader;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

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
    public static final int UPDATE_BROADCAST_REQUEST_CODE = 8832438;

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
        Intent intent = new Intent(context, UpdateWebcomsService.class);
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
        SharedPreferences globalPreferences = getSharedPreferences(UserRepository.GLOBAL_PREFERENCES, MODE_PRIVATE);
        String limit = globalPreferences.getString("download_limit", "no limit");

        //If updates are limited to wifi
        if(limit.equals("autodownloads updates over wifi") || limit.equals("all downloads over wifi")){
            ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if(!mWifi.isConnected()){
                return;
            }
        }

        if(!isLoop) {
            isLoop = true;
            final LiveData<List<ReadWebcom>> webcoms = readWebcomsDAO.getAll();
            webcoms.observeForever(new Observer<List<ReadWebcom>>() {
                @Override
                public void onChanged(@Nullable List<ReadWebcom> readWebcoms) {
                    webcoms.removeObserver(this);
                    Iterator<ReadWebcom> it = readWebcoms.iterator();
                    updateRec(it);
                }
            });

            SharedPreferences preferences = getSharedPreferences(UserRepository.GLOBAL_PREFERENCES, MODE_PRIVATE);
            boolean autoupdate = preferences.getBoolean("autoupdate", true);

            if(autoupdate) {
                int minutes = preferences.getInt("autoupdate_time", 120);

                Intent intent = new Intent(this, SheduledUpdateReceiver.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(), UPDATE_BROADCAST_REQUEST_CODE, intent, 0);
                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + minutes*60000, pendingIntent);
            }
        }
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
            webcom.setChaptersDAO(chaptersDAO);
            webcom.setReadWebcomsDAO(readWebcomsDAO);
            webcom.setChapterUpdateBroadcaster(UpdateWebcomsService.this);
            webcom.updateChapterList(new TaskDelegate(){
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
