package com.example.mymusicapp.Adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mymusicapp.Model.Music;
import com.example.mymusicapp.R;

import java.util.ArrayList;
import java.util.List;

public class RecycleViewAdapter extends RecyclerView.Adapter<RecycleViewAdapter.InnerHolder> {

    private List<Music> musicList;
    private OnItemViewClickListener clickListener;
    TextView songName;
    TextView singer;

    public RecycleViewAdapter() {
        musicList = new ArrayList<>();
    }

    @NonNull
    @Override
    public RecycleViewAdapter.InnerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
        return new InnerHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecycleViewAdapter.InnerHolder holder, int position) {
        holder.setData(musicList.get(position), position);
    }

    @Override
    public int getItemCount() {
        if (musicList != null)
            return musicList.size();
        else
            return 0;
    }

    public class InnerHolder extends RecyclerView.ViewHolder {
        private int mPosition;

        public InnerHolder(@NonNull View itemView) {
            super(itemView);
            songName = itemView.findViewById(R.id.item_song_name);
            singer = itemView.findViewById(R.id.item_singer_name);
            //给每一个itemView都是只长按和点击的监听器
            itemView.setOnClickListener(v -> {
                if (clickListener != null)
                    clickListener.onClick(mPosition);
            });
            itemView.setOnLongClickListener(v -> {
                if (clickListener != null)
                    clickListener.onLongClick(mPosition);
                return false;
            });
        }

        //给每一个ItemView设置数据
        public void setData(Music music, int position) {
            songName.setText(music.getName());
            singer.setText(music.getSinger());
            this.mPosition = position;
        }
    }

    //设置列表数据 并且更新
    @SuppressLint("NotifyDataSetChanged")
    public void setData(List<Music> list) {
        musicList.clear();
        musicList.addAll(list);
        notifyDataSetChanged();
    }

    //移除某一个itemView 并且更新
    public void removeItemView(int position) {
        musicList.remove(position);
        notifyItemRemoved(position);
        notifyItemChanged(0, getItemCount());
    }

    //设置监听器
    public void setClickListener(OnItemViewClickListener listener) {
        this.clickListener = listener;
    }

    public interface OnItemViewClickListener {
        void onClick(int position);

        void onLongClick(int position);
    }
}


