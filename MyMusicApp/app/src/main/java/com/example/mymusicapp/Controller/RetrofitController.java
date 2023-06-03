package com.example.mymusicapp.Controller;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @author Norton
 * @date 2023.6.1
 * @brief
 * Retrofit将网络请求转变成了Java interface的形式
 */
public class RetrofitController {
    // 基本请求来自网易云
    private static final Retrofit retrofit = new Retrofit.Builder()
            //设置基址
            .baseUrl("https://music.163.com")
            //添加转换器
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    public static Retrofit getRetrofit() {
        return retrofit;
    }
}