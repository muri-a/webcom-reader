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

    public abstract String getId();
    public abstract String getTitle();

    public abstract String getWebpage();
    public abstract String getDescription();
    public abstract int getIcon();
    public abstract format getFormat();
    public abstract String[] getTags();
    public abstract String[] getLanguages();
    public abstract ReadingOrder getReadingOrder();
    public abstract MutableLiveData<Integer> getChapterCount(); //Returns number of all available pages/chapters of comic
    public abstract boolean canOpenSource();

    public LiveData<String> getChapterUrl(String chapter){
        final MutableLiveData<String> chapterUrl = new MutableLiveData<>();
        final LiveData<ComicPage> call = getChapterMeta(chapter);
        call.observeForever(new Observer<ComicPage>() {
            @Override
            public void onChanged(@Nullable ComicPage comicPage) {
                call.removeObserver(this);
                if(comicPage != null)
                    chapterUrl.postValue(comicPage.getImage());
                else
                    chapterUrl.postValue("");
            }
        });
        return chapterUrl;
    }

    public abstract void updateChapterList(ChapterUpdateBroadcaster chapterUpdateBroadcaster, ChaptersDAO chaptersDAO, ReadWebcomsDAO readWebcomsDAO, TaskDelegate delegate);
    public abstract LiveData<ComicPage> getChapterMeta(String number);
    public abstract LiveData<Uri> getChapterSource(String chapterNumber);
    //public abstract List<String> getChapterList();
    //public String getFirstChapterId(){ return getChapterList().get(0); }
    //public String getLastChapterId(){ return getChapterList().get(getChapterList().size()-1); }
    //public abstract void updateChapters();
}
