package com.example.in9cow;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

import java.util.List;

public interface ApiService {
    @GET("congestion")
    Call<List<CongestionPoint>> getCongestionPoints();

    @GET("api/recommendation")
    Call<List<CongestionPoint>> getRecommendations(
            @Query("category") String category,
            @Query("sort") String sort
    );

    @POST("api/favorite/add")
    Call<Void> addFavorite(@Body FavoriteRequest body);

    @POST("api/favorite/remove")
    Call<Void> removeFavorite(@Body FavoriteRequest body);

    @GET("api/favorite/list")
    Call<List<CongestionPoint>> getFavoriteList(@Query("userId") long userId);

}

