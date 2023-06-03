package com.example.musicapp.Controller;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
/**
 * @auther Norton
 * @date 2023.5.28
 * @brief Retrofit控制器
 */
public class RetrofitController {
    private static final Retrofit retrofit = new Retrofit.Builder()
            // 设置基址
            .baseUrl("https://music.163.com")
            // 添加转换器
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    public static Retrofit getRetrofit() {
        return retrofit;
    }
}
