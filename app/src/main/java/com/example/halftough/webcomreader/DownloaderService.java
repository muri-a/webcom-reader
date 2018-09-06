package com.example.halftough.webcomreader;

import android.app.IntentService;
import android.arch.lifecycle.LiveData;
import android.content.Intent;
import android.content.Context;

import com.example.halftough.webcomreader.activities.ChapterList.ChapterListActivity;
import com.example.halftough.webcomreader.database.AppDatabase;
import com.example.halftough.webcomreader.database.Chapter;
import com.example.halftough.webcomreader.database.ChaptersDAO;
import com.example.halftough.webcomreader.webcoms.Webcom;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Subclass for handling downloads of webcomics in the background
 */
public class DownloaderService extends IntentService {
    private static final String ACTION_FOO = "com.example.halftough.webcomreader.action.FOO";
    private static final String ACTION_ENQUEUE_CHAPTER = "ACTION_ENQUEUE_CHAPTER";

    private static final String EXTRA_PARAM1 = "com.example.halftough.webcomreader.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "com.example.halftough.webcomreader.extra.PARAM2";

    OneByOneUrlDownloader downloader;
    private ChaptersDAO chaptersDAO;

    public DownloaderService() {
        super("DownloaderService");
        downloader = new ChapterDownloader();
        AppDatabase db = AppDatabase.getDatabase(this);
        chaptersDAO = db.chaptersDAO();
    }

    public static void startActionFoo(Context context, String param1, String param2) {
        Intent intent = new Intent(context, DownloaderService.class);
        intent.setAction(ACTION_FOO);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
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
            if (ACTION_FOO.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionFoo(param1, param2);
            } else if (ACTION_ENQUEUE_CHAPTER.equals(action)) {
                final String wid = intent.getStringExtra(ChapterListActivity.CHAPTER_WID);
                final String chapter = intent.getStringExtra(ChapterListActivity.CHAPTER_NUMBER);
                handleEnqueueChapter(wid, chapter);
            }
        }
    }

    private void handleActionFoo(String wid, String chapter) {
        // TODO: Handle action Foo
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void handleEnqueueChapter(final String wid, final String chapter) {
        try {
            Webcom webcom = UserRepository.getWebcomInstance(wid);
            LiveData<String> url = webcom.getChapterUrl(chapter);
            new OnLiveDataReady<String>(){
                @Override
                public void onReady(String value) {
                    downloader.enqueue(value, new Chapter(wid, chapter));
                }
            }.run(url);
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
