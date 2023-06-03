package com.example.musicapp.NetWork;

import com.example.musicapp.Model.Music;
import com.example.musicapp.Model.MusicDetail;
import com.example.musicapp.Model.MusicSearch;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface NetworkRequest {
    @POST
    Call<MusicSearch> getSearchResult(@Url String url);

    @GET("/api/song/enhance/player/url")
    Call<MusicDetail> getMusicDetail(@Query("id") String id, @Query("ids") String ids, @Query("br") int br);

    @GET
    Call<ResponseBody> downloadFile(@Url String url, @Header("RANGE") long position);
}
