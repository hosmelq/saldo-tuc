package com.socialimprover.saldotuc.api;

import com.socialimprover.saldotuc.api.model.MpesoCard;

import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import rx.Observable;

public interface MpesoService {
    String SERVICE_ENDPOINT = "https://mpeso.net/";

    @FormUrlEncoded
    @POST("datos/consulta.php")
    Observable<MpesoCard> getBalance(
        @Field("_captcha") String captcha,
        @Field("_codigo") String codigo,
        @Field("_funcion") String function,
        @Field("_terminal") String terminal
    );
}
