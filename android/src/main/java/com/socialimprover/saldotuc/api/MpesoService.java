package com.socialimprover.saldotuc.api;

import com.socialimprover.saldotuc.api.model.MpesoCard;

import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;
import rx.Observable;

public interface MpesoService {
    String SERVICE_ENDPOINT = "https://mpeso.net/";

    @FormUrlEncoded
    @POST("datos/consulta.php")
    Observable<MpesoCard> getBalance(@Field("_funcion") String function, @Field("_terminal") String terminal);
}
