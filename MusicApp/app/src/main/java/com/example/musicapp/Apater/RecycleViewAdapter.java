package com.example.musicapp.Apater;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicapp.Controller.ItemClickListener;
import com.example.musicapp.Model.Music;
import com.example.musicapp.R;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Norton
 * @date 2023.5.28
 * @brief RecycleView适配器
 */
public class RecycleViewAdapter extends RecyclerView.Adapter<RecycleViewAdapter.InnerHolder> {

    private List<Music> musicList;
    private ItemClickListener listener;
    TextView song_name;
    TextView singer;

    public RecycleViewAdapter() {
        musicList = new ArrayList<>();
    }

    @NonNull
    @Override
    public RecycleViewAdapter.InnerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.result_list_item_view, parent, false);
        return new InnerHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecycleViewAdapter.InnerHolder holder, int position) {
        holder.setItem(musicList.get(position), position);
    }

    @Override
    public int getItemCount() {
        return musicList == null?0: musicList.size();
    }

    public class InnerHolder extends RecyclerView.ViewHolder {
        private int position;

        public InnerHolder(@NonNull View itemView) {
            super(itemView);
            song_name = itemView.findViewById(R.id.result_list_item_song_name);
            singer = itemView.findViewById(R.id.result_list_item_singer);
            // 给每一个itemView都是只长按和点击的监听器
            itemView.setOnClickListener(v -> {
                if (listener != null)
                    listener.onClick(position);
            });
            itemView.setOnLongClickListener(v -> {
                if (listener != null)
                    listener.onLongClick(position);
                return false;
            });
        }

        // 给每一个ItemView设置数据
        public void setItem(Music music, int position) {
            song_name.setText(music.getName());
            singer.setText(music.getSinger());
            this.position = position;
        }
    }

    // 设置列表数据 并且更新
    @SuppressLint("NotifyDataSetChanged")
    public void setItem(List<Music> list) {
        musicList.clear();
        musicList.addAll(list);
        notifyDataSetChanged();
    }

    // 移除某一个itemView 并且更新
    public void removeItem(int position) {
        musicList.remove(position);
        notifyItemRemoved(position);
        notifyItemChanged(0, getItemCount());
    }

    // 设置监听器
    public void setListener(ItemClickListener listener) {
        this.listener = listener;
    }


}
