package com.example.musicapp.Controller;
/**
 * @author Norton-Lin
 * @date 2023.6.2
 * @brief 条目点击监听接口
 */
public interface ItemClickListener {
    void onClick(int position);

    void onLongClick(int position);
}
