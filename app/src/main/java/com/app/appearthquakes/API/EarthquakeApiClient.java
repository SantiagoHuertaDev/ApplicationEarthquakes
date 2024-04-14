package com.app.appearthquakes.API;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class EarthquakeApiClient {

    private static final String BASE_URL = "https://earthquake.usgs.gov/fdsnws/event/1/query";
    private static final String PARAM_FORMAT = "format";
    private static final String PARAM_START_TIME = "starttime";
    private static final String PARAM_END_TIME = "endtime";

    private final OkHttpClient client;

    public EarthquakeApiClient() {
        client = new OkHttpClient();
    }

    public void fetchEarthquakeData(String startTime, String endTime, Callback callback) {
        String url = buildUrl(startTime, endTime);

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(callback);
    }

    private String buildUrl(String startTime, String endTime) {
        return BASE_URL + "?" +
                PARAM_FORMAT + "=geojson&" +
                PARAM_START_TIME + "=" + startTime + "&" +
                PARAM_END_TIME + "=" + endTime;
    }
}
