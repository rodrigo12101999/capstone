package com.upn.chapanomas.retrofit;

import com.upn.chapanomas.clases.FCMBody;
import com.upn.chapanomas.clases.FCMResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMApi {

    @Headers({
      "Content-Type:application/json", "Authorization:key=AAAALwftboY:APA91bHopJmyP1pqniSUZuPluYNXdKUGUy7NFM08YU7l1j3Jickig3f-uknYMZGpzbUQLdOS1E1PkXMeRiav6ygEiArYw119wpLEl6Kn1FDQAw0Oa1J4ZuYEavog8WvwAJiaKINgF7SK"
    })
    @POST("fcm/send")
    Call<FCMResponse> send(@Body FCMBody body);
}
