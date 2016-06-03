package com.socialimprover.saldotuc.api;

import com.socialimprover.saldotuc.api.model.Balance;

import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

public interface MpesoService {
    String SERVICE_ENDPOINT = "http://mpeso.saldotuc.com/";

    @GET("cards/{number}/balance")
    Observable<Balance> getBalance(@Path("number") String number);
}
