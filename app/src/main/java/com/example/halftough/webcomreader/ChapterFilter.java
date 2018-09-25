package com.example.halftough.webcomreader;

import com.example.halftough.webcomreader.database.Chapter;

public class ChapterFilter {
    private boolean read = false;
    private boolean unread = false;
    private boolean downloaded = false;
    private boolean undownloaded = false;

    public boolean isRead() {
        return read;
    }
    public void setRead(boolean read) {
        this.read = read;
    }
    public boolean isUnread() {
        return unread;
    }
    public void setUnread(boolean unread) {
        this.unread = unread;
    }
    public boolean isDownloaded() {
        return downloaded;
    }
    public void setDownloaded(boolean downloaded) {
        this.downloaded = downloaded;
    }
    public boolean isUndownloaded() {
        return undownloaded;
    }
    public void setUndownloaded(boolean undownloaded) {
        this.undownloaded = undownloaded;
    }

    public boolean allows(Chapter chapter) {
        //this could be one liner, but this way it's more readable
        if(read && chapter.getStatus()!= Chapter.Status.READ)
            return false;
        if(unread && chapter.getStatus()!= Chapter.Status.UNREAD)
            return false;
        if(downloaded && chapter.getDownloadStatus()!= Chapter.DownloadStatus.DOWNLOADED)
            return false;
        if(undownloaded && chapter.getDownloadStatus() == Chapter.DownloadStatus.DOWNLOADED)
            return false;
        return true;
    }
}
