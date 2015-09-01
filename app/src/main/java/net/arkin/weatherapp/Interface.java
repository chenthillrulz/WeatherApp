package net.arkin.weatherapp;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by Chenthill on 9/2/2015.
 */
public interface Interface {
    @GET("/weather")
    void getWeatherData (@Query("q") String cityName, Callback<WeatherData> cb);
}
