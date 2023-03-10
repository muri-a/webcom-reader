package com.example.halftough.webcomreader.activities.Library;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.halftough.webcomreader.PreferenceHelper;
import com.example.halftough.webcomreader.R;
import com.example.halftough.webcomreader.UserRepository;
import com.example.halftough.webcomreader.database.ReadWebcom;
import com.example.halftough.webcomreader.webcoms.Webcom;

import java.util.List;

public class LibraryAdapter extends RecyclerView.Adapter<LibraryAdapter.ViewHolder>{

    class ViewHolder extends RecyclerView.ViewHolder{
        public View item;
        public TextView nameTextView;
        public TextView unreadMarker;
        public ImageView iconView;

        public ViewHolder(View itemView){
            super(itemView);
            item = itemView;
            nameTextView = (TextView)itemView.findViewById(R.id.myWebcomTitle);
            iconView = (ImageView)itemView.findViewById(R.id.myWebcomIcon);
            unreadMarker = (TextView)itemView.findViewById(R.id.myWebcomUnread);
        }
    }

    private final LayoutInflater mInflater;
    private List<ReadWebcom> readWebcoms;
    private LibraryActivity context;

    public LibraryAdapter(LibraryActivity context){
        this.context = context;
        mInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        SharedPreferences preferences = context.getSharedPreferences(UserRepository.GLOBAL_PREFERENCES, Context.MODE_PRIVATE);
        View itemView;
        if(preferences.getString("library_style", context.getString(R.string.global_preferences_librery_style_default)).equals("list")) {
            itemView = mInflater.inflate(R.layout.library_list_item, parent, false);
        }
        else{
            itemView = mInflater.inflate(R.layout.library_grid_item, parent, false);
        }
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if(readWebcoms != null){
            final ReadWebcom readWebcom = readWebcoms.get(position);
            final Webcom webcom = UserRepository.getWebcomInstance(readWebcom.getWid());

            holder.nameTextView.setText(webcom.getTitle());
            holder.iconView.setImageDrawable(context.getResources().getDrawable(webcom.getIcon()));
            int unread = readWebcom.getChapterCount()-readWebcom.getReadChapters();
            if(unread == 0){
                holder.unreadMarker.setVisibility(View.GONE);
            }
            else {
                holder.unreadMarker.setText(Integer.toString(unread));
                holder.unreadMarker.setVisibility(View.VISIBLE);
            }
            SharedPreferences preferences = context.getSharedPreferences(UserRepository.GLOBAL_PREFERENCES, Context.MODE_PRIVATE);
            //TODO being lazy
            if(preferences.getString("library_style", context.getString(R.string.global_preferences_librery_style_default)).equals("grid")){
                Point size = new Point();
                DisplayMetrics metrics = new DisplayMetrics();
                context.getWindowManager().getDefaultDisplay().getSize(size);
                context.getWindowManager().getDefaultDisplay().getMetrics(metrics);
                float width = size.x/PreferenceHelper.getCurrentGridCols(context, preferences) - 30*metrics.density;
                holder.iconView.getLayoutParams().height = (int)width;
            }
            if(context.getSelectedWebcoms().contains(readWebcom)){
                holder.item.setBackground(context.getResources().getDrawable(R.drawable.library_entry_background_selected));
            }
            else {
                holder.item.setBackgroundResource(0);
            }
            holder.item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(context.getMode() == LibraryActivity.ActivityMode.NORMAL) {
                        context.showChapterList(readWebcom.getWid());
                    }
                    else{
                        triggerSelected(v, readWebcom);
                    }
                }
            });
            holder.item.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    triggerSelected(v, readWebcom);
                    return true;
                }
            });
        }
    }

    private void triggerSelected(View v, ReadWebcom webcom){
        if( context.triggerChapterSelect(webcom) ){
            v.setBackground(context.getResources().getDrawable(R.drawable.library_entry_background_selected));
        }
        else{
            v.setBackgroundResource(0);
        }
    }

    void setReadWebcoms(List<ReadWebcom> readWebcoms){
        this.readWebcoms = readWebcoms;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount(){
        if(readWebcoms!=null){
            return readWebcoms.size();
        }
        return 0;
    }
}
