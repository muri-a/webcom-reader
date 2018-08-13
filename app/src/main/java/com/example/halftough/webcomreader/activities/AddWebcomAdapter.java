package com.example.halftough.webcomreader.activities;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.halftough.webcomreader.R;
import com.example.halftough.webcomreader.webcoms.Webcom;

import java.util.List;

public class AddWebcomAdapter extends RecyclerView.Adapter<AddWebcomAdapter.ViewHolder> {

    private List<Webcom> items;
    private Context context;

    class ViewHolder extends RecyclerView.ViewHolder{
        public TextView nameTextView;
        public ImageView iconView;

        public ViewHolder(View itemView){
            super(itemView);
            nameTextView = (TextView)itemView.findViewById(R.id.availableWebcomTitle);
            iconView = (ImageView)itemView.findViewById(R.id.availableWebcomIcon);
        }
    }

    public AddWebcomAdapter(List<Webcom> list){
        this.items = list;
    }

    @NonNull
    @Override
    public AddWebcomAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        context = viewGroup.getContext();
        LayoutInflater inflayer = LayoutInflater.from(context);

        View view = inflayer.inflate(R.layout.add_webcom_list_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddWebcomAdapter.ViewHolder viewHolder, int position) {
        Webcom item = items.get(position);
        viewHolder.nameTextView.setText(item.getTitle());
        viewHolder.iconView.setImageDrawable(context.getResources().getDrawable(item.getIcon()));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

}
