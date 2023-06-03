package com.example.musicapp.Apater;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
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

    private List<Music> mData;
    private OnItemViewClickListener itemViewClickListener;
    TextView tx_songName;
    TextView tx_artist;

    public RecycleViewAdapter() {
        mData = new ArrayList<>();
    }

    @NonNull
    @Override
    public RecycleViewAdapter.InnerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.result_list_item_view, parent, false);
        return new InnerHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecycleViewAdapter.InnerHolder holder, int position) {
        holder.setData(mData.get(position), position);
    }

    @Override
    public int getItemCount() {
        if (mData != null)
            return mData.size();
        else
            return 0;
    }

    public class InnerHolder extends RecyclerView.ViewHolder {
        private int mPosition;

        public InnerHolder(@NonNull View itemView) {
            super(itemView);
            tx_songName = itemView.findViewById(R.id.tx_result_list_item_song_name);
            tx_artist = itemView.findViewById(R.id.tx_result_list_item_artist);
            // 给每一个itemView都是只长按和点击的监听器
            itemView.setOnClickListener(v -> {
                if (itemViewClickListener != null)
                    itemViewClickListener.onClick(mPosition);
            });
            itemView.setOnLongClickListener(v -> {
                if (itemViewClickListener != null)
                    itemViewClickListener.onLongClick(mPosition);
                return false;
            });
        }

        // 给每一个ItemView设置数据
        public void setData(Music data, int position) {
            tx_songName.setText(data.getName());
            tx_artist.setText(data.getSinger());
            this.mPosition = position;
        }
    }

    // 设置列表数据 并且更新
    @SuppressLint("NotifyDataSetChanged")
    public void setData(List<Music> list) {
        mData.clear();
        mData.addAll(list);
        notifyDataSetChanged();
    }

    // 移除某一个itemView 并且更新
    public void removeItemView(int position) {
        mData.remove(position);
        notifyItemRemoved(position);
        notifyItemChanged(0, getItemCount());
    }

    // 设置监听器
    public void setItemViewClickListener(OnItemViewClickListener listener) {
        this.itemViewClickListener = listener;
    }

    public interface OnItemViewClickListener {
        void onClick(int position);

        void onLongClick(int position);
    }
}
