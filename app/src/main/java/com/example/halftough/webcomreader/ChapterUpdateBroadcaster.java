package com.example.halftough.webcomreader;

import com.example.halftough.webcomreader.database.Chapter;

public interface ChapterUpdateBroadcaster {
    public void broadcastChapterUpdated(Chapter chapter);
}
