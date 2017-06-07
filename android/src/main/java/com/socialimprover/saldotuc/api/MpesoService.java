package com.socialimprover.saldotuc.api;

import com.socialimprover.saldotuc.api.model.Balance;

import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

public interface MpesoService {
    String SERVICE_ENDPOINT = "http://balance.saldotuc.com/";

    @GET("{number}")
    Observable<Balance> getBalance(@Path("number") String number);
}
