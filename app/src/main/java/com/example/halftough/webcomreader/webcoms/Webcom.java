package com.example.halftough.webcomreader.webcoms;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.example.halftough.webcomreader.ChapterUpdateBroadcaster;
import com.example.halftough.webcomreader.TaskDelegate;
import com.example.halftough.webcomreader.database.ChaptersDAO;
import com.example.halftough.webcomreader.database.ReadWebcomsDAO;

public abstract class Webcom {
    public enum format { CHAPTERS, PAGES;}

    public enum ReadingOrder {OLDEST_FIRST, NEWEST_FIRST;}
    protected ChaptersDAO chaptersDAO;
    protected ReadWebcomsDAO readWebcomsDAO;
    protected ChapterUpdateBroadcaster chapterUpdateBroadcaster;

    public abstract String getId();
    public abstract String getTitle();

    public abstract String getWebpage();
    public abstract String getDescription();
    public abstract int getIcon();
    public abstract format getFormat();
    public abstract String[] getTags();
    public abstract String[] getLanguages();
    public abstract ReadingOrder getReadingOrder();
    public abstract boolean canOpenSource();

    public void setChaptersDAO(ChaptersDAO chaptersDAO){
        this.chaptersDAO = chaptersDAO;
    }
    public void setReadWebcomsDAO(ReadWebcomsDAO readWebcomsDAO){
        this.readWebcomsDAO = readWebcomsDAO;
    }
    public void setChapterUpdateBroadcaster(ChapterUpdateBroadcaster chapterUpdateBroadcaster) {
        this.chapterUpdateBroadcaster = chapterUpdateBroadcaster;
    }

    public abstract LiveData<String> getChapterUrl(String chapter);

    public abstract void updateChapterList(TaskDelegate delegate);
    //public abstract LiveData<ComicPage> getChapterPage(String number);
    public abstract LiveData<Uri> getChapterSource(String chapterNumber);
}
