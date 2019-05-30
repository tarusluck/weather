package com.deskcode.forecasting.network;

import com.deskcode.forecasting.models.ForecastModel;
import com.deskcode.forecasting.models.Weather;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface APIInterface {

    @GET("weather")
    Call<ForecastModel> getForecastData(@Query("lat") int lat, @Query("lon") int lon, @Query("appid") String appid);
//    Call<ForecastModel> getForecastData(@Query("lat") int lat, @Query("lon") int lon);
}
