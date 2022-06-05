package com.upn.chapanomas.retrofit;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;


public interface IGoogleAPI {
    @GET
    Call<String> getDirections(@Url String url);
}
