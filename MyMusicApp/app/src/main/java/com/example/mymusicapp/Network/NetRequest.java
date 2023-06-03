package com.example.mymusicapp.Network;

import com.example.mymusicapp.Model.MusicDetail;
import com.example.mymusicapp.Model.MusicResult;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.Url;

/**
 * @author Norton
 * @date 2023.6.2
 * @brief 网络请求API
 *  retrofit真好用
 */
public interface NetRequest {
    @POST
    Call<MusicResult> getSearchResult(@Url String url);

    @GET("/api/song/enhance/player/url")
    Call<MusicDetail> getMusicDetail(@Query("id") String id, @Query("ids") String ids, @Query("br") int br);

    @GET
    Call<ResponseBody> downloadFile(@Url String url, @Header("RANGE") long position);
}
