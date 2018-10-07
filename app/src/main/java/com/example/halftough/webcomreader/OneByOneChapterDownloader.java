package com.example.halftough.webcomreader;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.ContextWrapper;
import android.support.annotation.Nullable;

import com.example.halftough.webcomreader.database.Chapter;
import com.example.halftough.webcomreader.webcoms.ComicPage;
import com.example.halftough.webcomreader.webcoms.Webcom;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.Queue;

//Class preventing app from asking server for huge number of calls at once
public abstract class OneByOneChapterDownloader extends OneByOneDownloader<String, Void> {
    private Webcom webcom;
    private WeakReference<DownloaderService> downloaderService;
    private final int refreshRate = 5;
    private int refreshCounter = 0;

    public OneByOneChapterDownloader(Webcom webcom, DownloaderService service){ this(new LinkedList<String>(), webcom, service); }

    public OneByOneChapterDownloader(Queue<String> chapters, Webcom webcom, DownloaderService service){
        this(chapters, webcom, service, 1);
    }

    public OneByOneChapterDownloader(Queue<String> chapters, Webcom webcom, DownloaderService service, int slots){
        free = capacity = slots;
        queue = chapters;
        this.webcom = webcom;
        downloaderService = new WeakReference<>(service);
    }

    public abstract void onResponse(ComicPage page);

    protected void downloadElement(final String element, final Void extra){
        final LiveData<ComicPage> page = webcom.getChapterMeta(element);
        page.observeForever(new Observer<ComicPage>() {
            @Override
            public void onChanged(@Nullable ComicPage comicPage) {
                page.removeObserver(this);
                OneByOneChapterDownloader.this.onResponse(comicPage);
                elementDownloaded();
                refreshCounter++;
                if(refreshCounter % refreshRate == 0 || !downloading){
                    downloaderService.get().broadcastChapterUpdated(new Chapter(webcom.getId(), element));
                }
            }
        });
    }
}
