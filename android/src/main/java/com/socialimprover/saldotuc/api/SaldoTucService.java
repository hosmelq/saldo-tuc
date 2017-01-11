package com.socialimprover.saldotuc.api;

import com.socialimprover.saldotuc.api.model.Balance;
import com.socialimprover.saldotuc.api.model.Neighborhood;
import com.socialimprover.saldotuc.api.model.Notification;

import java.util.List;

import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import rx.Observable;

public interface SaldoTucService {
    String SERVICE_ENDPOINT = "https://api.saldotuc.com/v1/";

    @FormUrlEncoded
    @POST("cards/{number}/balances")
    Observable<Balance> storeBalance(@Path("number") String number, @Field("balance") String balance);

    @GET("cards/{number}/balances")
    Observable<List<Balance>> balances(@Path("number") String number);

    @PUT("cards/{number}/notifications/{id}")
    Observable<Notification> notificationReceived(@Path("number") String number, @Path("id") int id);

    @GET("neighborhoods?include=agencies")
    Observable<List<Neighborhood>> neighborhoods();
}
