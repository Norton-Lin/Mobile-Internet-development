package com.example.musicapp.NetWork;

import com.example.musicapp.Model.MusicList;
import com.example.musicapp.Model.MusicSearch;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.Url;
/**
 * @auther Norton-Lin
 * @date 2023.5.28
 * @brief 网络请求接口
 */
public interface NetworkRequest {
    @POST
    Call<MusicSearch> getSearchResult(@Url String url);

    @GET("/api/song/enhance/player/url")
    Call<MusicList> getMusicDetail(@Query("id") String id, @Query("ids") String ids, @Query("br") int br);

    @GET
    Call<ResponseBody> downloadFile(@Url String url, @Header("RANGE") long position);
}
