package com.example.halftough.webcomreader.activities.ChapterList;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.halftough.webcomreader.R;
import com.example.halftough.webcomreader.database.Chapter;

import java.util.List;

public class ChapterListAdapter extends RecyclerView.Adapter<ChapterListAdapter.ViewHolder>{

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView chapterNumber;
        public TextView chapterTitle;

        public ViewHolder(View itemView) {
            super(itemView);
            chapterNumber = (TextView)itemView.findViewById(R.id.chapterListItemNumber);
            chapterTitle = (TextView)itemView.findViewById(R.id.chapterListItemTitle);
        }
    }

    private final LayoutInflater mInflater;
    List<Chapter> chapters;

    public ChapterListAdapter(Context context){
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
        if(chapters!=null) {
            Chapter chapter = chapters.get(position);
            holder.chapterNumber.setText(new Integer(chapter.getChapter()).toString());
            holder.chapterTitle.setText(chapter.getTitle());
        }
    }

    @Override
    public int getItemCount() {
        if(chapters != null){
            return chapters.size();
        }
        return 0;
    }

    void setChapters(List<Chapter> chapters){
        this.chapters = chapters;
        notifyDataSetChanged();
    }
}
