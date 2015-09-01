package net.arkin.weatherapp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

/**
 * Created by Chenthill on 9/2/2015.
 */
public class RestClient {
    public static final String BASE_URL = "http://api.openweathermap.org/data/2.5";
    private Interface apiService;

    public RestClient ()
    {
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setEndpoint(BASE_URL)
                .setConverter(new GsonConverter(gson))
                .build();

        apiService = restAdapter.create(Interface.class);
    }

    public Interface getApiService()
    {
        return apiService;
    }
}
