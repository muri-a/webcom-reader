package com.example.halftough.webcomreader.activities.ChapterList;

import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.halftough.webcomreader.ChapterFilter;
import com.example.halftough.webcomreader.R;
import com.example.halftough.webcomreader.database.Chapter;

import java.util.ArrayList;
import java.util.List;

public class ChapterListAdapter extends RecyclerView.Adapter<ChapterListAdapter.ViewHolder>{

    public class ViewHolder extends RecyclerView.ViewHolder{

        public View item;
        public TextView chapterNumber;
        public TextView chapterTitle;
        public TextView downloadedText;
        public ImageButton menuButton;
        public ViewHolder(View itemView) {
            super(itemView);
            chapterNumber = (TextView)itemView.findViewById(R.id.chapterListItemNumber);
            chapterTitle = (TextView)itemView.findViewById(R.id.chapterListItemTitle);
            menuButton = (ImageButton)itemView.findViewById(R.id.chapterListItemMenuButton);
            downloadedText = (TextView)itemView.findViewById(R.id.chapterListItemDownloaded);
            item = itemView;
        }

    }
    private final LayoutInflater mInflater;

    private List<Chapter> chapters, filtered;
    private ChapterFilter filter;
    private ChapterListActivity context;
    public ChapterListAdapter(ChapterListActivity context){
        this.context = context;
        mInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.chapter_list_item, parent, false);
        return new ChapterListAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if(filtered != null) {
            final Chapter chapter = filtered.get(position);
            if(chapter.getStatus() == Chapter.Status.READ){
                holder.chapterNumber.setTextColor(ContextCompat.getColor(context, R.color.chapterRead));
                holder.chapterTitle.setTextColor(ContextCompat.getColor(context, R.color.chapterRead));
            }
            else{
                holder.chapterNumber.setTextColor(ContextCompat.getColor(context, R.color.chapterUnread));
                holder.chapterTitle.setTextColor(ContextCompat.getColor(context, R.color.chapterUnread));
            }
            switch (chapter.getDownloadStatus()){
                case DOWNLOADED:
                    holder.downloadedText.setText(R.string.chapter_item_downloaded);
                    break;
                case DOWNLOADING:
                    holder.downloadedText.setText(R.string.chapter_item_downloading);
                    break;
                case UNDOWNLOADED:
                    holder.downloadedText.setText("");
            }
            holder.chapterNumber.setText(chapter.getChapter());
            holder.chapterTitle.setText(chapter.getTitle());
            holder.menuButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu menu = new PopupMenu(context, v);
                    MenuInflater menuInflater = menu.getMenuInflater();
                    menuInflater.inflate(R.menu.chapter_item_menu, menu.getMenu());
                    switch (chapter.getDownloadStatus()){
                        case UNDOWNLOADED:
                            menu.getMenu().findItem(R.id.chapterItemMenuDownload).setVisible(true);
                            menu.getMenu().findItem(R.id.chapterItemMenuRemove).setVisible(false);
                            break;
                        case DOWNLOADING:
                            menu.getMenu().findItem(R.id.chapterItemMenuDownload).setVisible(false);
                            menu.getMenu().findItem(R.id.chapterItemMenuRemove).setVisible(false);
                            break;
                        case DOWNLOADED:
                            menu.getMenu().findItem(R.id.chapterItemMenuDownload).setVisible(false);
                            menu.getMenu().findItem(R.id.chapterItemMenuRemove).setVisible(true);
                    }
                    menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        Chapter chap = chapter;
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()){
                                case R.id.chapterItemMenuDownload:
                                    context.downloadChapter(chap);
                                    return true;
                                case R.id.chapterItemMenuRemove:
                                    // TODO implement manual removing
                                    return true;
                                case R.id.chapterItemMenuMarkRead:
                                    context.getViewModel().markRead(chap);
                                    return true;
                                case R.id.chapterItemMenuMarkPreviousRead:
                                    context.getViewModel().markReadTo(chap);
                                    return true;
                                case R.id.chapterItemMenuMarkLaterRead:
                                    context.getViewModel().markReadFrom(chap);
                                    return true;
                                case R.id.chapterItemMenuMarkUnread:
                                    context.getViewModel().markUnread(chap);
                                    return true;
                                case R.id.chapterItemMenuMarkPreviousUnread:
                                    context.getViewModel().markUnreadTo(chap);
                                    return true;
                                case R.id.chapterItemMenuMarkLaterUndear:
                                    context.getViewModel().markUnreadFrom(chap);
                                    return true;
                            }
                            return false;
                        }
                    });
                    menu.show();
                }
            });
            holder.item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    context.readWebcom(chapter);
                }
            });

        }
    }

    @Override
    public int getItemCount() {
        if(filtered != null){
            return filtered.size();
        }
        return 0;
    }

    void changeFilter(ChapterFilter filter){
        this.filter = filter;
        filterChapters();
        notifyDataSetChanged();
    }

    void clearFilter(){
        filter = null;
        setChapters(chapters);
    }

    void setChapters(List<Chapter> chapters){
        this.chapters = chapters;
        if(filter == null){
            filtered = chapters;
        }
        else{
            filterChapters();
        }
        notifyDataSetChanged();
    }

    private void filterChapters(){
        if(chapters == null)
            return;
        filtered = new ArrayList<>();
        for(Chapter chapter : chapters){
            if(filter.allows(chapter)){
                filtered.add(chapter);
            }
        }
    }

}
