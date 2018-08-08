package com.example.halftough.webcomreader.activities.MyWebcoms;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.halftough.webcomreader.NoWebcomClassException;
import com.example.halftough.webcomreader.R;
import com.example.halftough.webcomreader.UserRepository;
import com.example.halftough.webcomreader.database.ReadWebcoms;
import com.example.halftough.webcomreader.webcoms.Webcom;

import java.util.List;

public class MyWebcomsAdapter extends RecyclerView.Adapter<MyWebcomsAdapter.ViewHolder>{

    class ViewHolder extends RecyclerView.ViewHolder{
        public TextView nameTextView;
        public ImageView iconView;

        public ViewHolder(View itemView){
            super(itemView);
            nameTextView = (TextView)itemView.findViewById(R.id.myWebcomTitle);
            iconView = (ImageView)itemView.findViewById(R.id.myWebcomIcon);
        }
    }

    private final LayoutInflater mInflater;
    private List<ReadWebcoms> readWebcoms;
    Context context;

    public MyWebcomsAdapter(Context context){
        this.context = context;
        mInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.my_webcoms_list_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if(readWebcoms != null){
            ReadWebcoms readWebcom = readWebcoms.get(position);
            try {
                Webcom webcom = UserRepository.getWebcomInstance(readWebcom.getWid());
                holder.nameTextView.setText(webcom.getTitle());
                holder.iconView.setImageDrawable(context.getResources().getDrawable(webcom.getIcon()));
            } catch (NoWebcomClassException e) {
                e.printStackTrace();
            }
        }
    }

    void setReadWebcoms(List<ReadWebcoms> readWebcoms){
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
