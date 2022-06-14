package com.upn.chapanomas.providers;

import com.upn.chapanomas.clases.FCMBody;
import com.upn.chapanomas.clases.FCMResponse;
import com.upn.chapanomas.retrofit.IFCMApi;
import com.upn.chapanomas.retrofit.RetrofitClient;

import retrofit2.Call;

public class NotificationProvider {
    private String url = "https://fcm.googleapis.com";

    public NotificationProvider() {
    }

    public Call<FCMResponse> sendNotification(FCMBody body){
        return RetrofitClient.getClientObject(url).create(IFCMApi.class).send(body);
    }
}
