package com.teamblue.WeBillv2.service;

import com.teamblue.WeBillv2.model.pojo.Constants;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class VeryfiRetrofitClient {
    private static Retrofit retrofit;
    private static String BASE_URL = Constants.VERYFI_API_URL;
    public static  OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
            .connectTimeout(240, TimeUnit.SECONDS)
            .readTimeout(240, TimeUnit.SECONDS)
            .writeTimeout(240, TimeUnit.SECONDS)
            .build();
    public static Retrofit getRetrofitInstance(){
        if(retrofit==null){
            retrofit = new retrofit2.Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build();
        }
        return retrofit;
    }
}
