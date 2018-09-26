package com.example.halftough.webcomreader.activities.MyWebcoms;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.halftough.webcomreader.R;
import com.example.halftough.webcomreader.UserRepository;
import com.example.halftough.webcomreader.database.ReadWebcom;
import com.example.halftough.webcomreader.webcoms.Webcom;

import java.util.List;

public class MyWebcomsAdapter extends RecyclerView.Adapter<MyWebcomsAdapter.ViewHolder>{

    class ViewHolder extends RecyclerView.ViewHolder{
        public TextView nameTextView;
        public TextView unreadMarker;
        public ImageView iconView;

        public ViewHolder(View itemView){
            super(itemView);
            nameTextView = (TextView)itemView.findViewById(R.id.myWebcomTitle);
            iconView = (ImageView)itemView.findViewById(R.id.myWebcomIcon);
            unreadMarker = (TextView)itemView.findViewById(R.id.myWebcomUnread);
        }
    }

    private final LayoutInflater mInflater;
    private List<ReadWebcom> readWebcoms;
    Context context;

    public MyWebcomsAdapter(Context context){
        this.context = context;
        mInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        SharedPreferences preferences = context.getSharedPreferences(UserRepository.GLOBAL_PREFERENCES, Context.MODE_PRIVATE);
        View itemView;
        if(preferences.getString("library_style", context.getString(R.string.global_preferences_librery_style_default)).equals("list")) {
            itemView = mInflater.inflate(R.layout.my_webcoms_list_item, parent, false);
        }
        else{
            itemView = mInflater.inflate(R.layout.my_webcoms_grid_item, parent, false);
        }
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if(readWebcoms != null){
            ReadWebcom readWebcom = readWebcoms.get(position);
            Webcom webcom = UserRepository.getWebcomInstance(readWebcom.getWid());
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
