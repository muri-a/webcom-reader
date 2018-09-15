package com.example.halftough.webcomreader.activities.ChapterList;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import com.example.halftough.webcomreader.DownloaderService;
import com.example.halftough.webcomreader.database.Chapter;
import com.example.halftough.webcomreader.database.ChaptersRepository;

import java.util.List;

public class ChapterListViewModel extends AndroidViewModel {
    private ChaptersRepository chaptersRepository;
    private MutableLiveData<List<Chapter>> chapters;
    private Application application;

    public ChapterListViewModel(Application application) {
        super(application);
        this.application = application;
    }

    public void setWid(String wid){
        chaptersRepository = new ChaptersRepository(application, wid);
        chapters = chaptersRepository.getChapters();
    }

    public Chapter getChapterToRead() {
        return chaptersRepository.getChapterToRead();
    }

    public void downloadNextChapters(int number) {
        List<Chapter> chapters = chaptersRepository.getChaptersToDownload(number);
        for (Chapter chapter : chapters) {
            downloadChapter(chapter);
        }
    }


    public LiveData<List<Chapter>> getChapters() {
        return chapters;
    }

    public void downloadChapter(Chapter chapter){
        chaptersRepository.downloadChapter(chapter);
    }

    public void update() {
        chaptersRepository.update();
    }

    public void markRead(Chapter chapter){
        chaptersRepository.markRead(chapter);
    }

    public void markReadTo(Chapter chapter){
        chaptersRepository.markReadTo(chapter);
    }

    public void markReadFrom(Chapter chapter){
        chaptersRepository.markReadFrom(chapter);
    }

    public void markUnread(Chapter chapter){
        chaptersRepository.markUnread(chapter);
    }

    public void markUnreadTo(Chapter chapter){
        chaptersRepository.markUnreadTo(chapter);
    }

    public void markUnreadFrom(Chapter chapter){
        chaptersRepository.markUnreadFrom(chapter);
    }
}
