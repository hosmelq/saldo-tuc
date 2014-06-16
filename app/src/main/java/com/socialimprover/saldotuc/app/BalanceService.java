package com.socialimprover.saldotuc.app;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

public class BalanceService {

    private static final String MPESO_URL = "https://mpeso.net/";

    public interface BalanceServiceInterface {

        @FormUrlEncoded
        @POST("/datos/consulta.php")
        public void getBalanceAsync(
            @Field("_funcion") String _funcion,
            @Field("_terminal") String _terminal,
            Callback<Balance> callback
        );

    }

    public void loadBalance(Card card, Callback<Balance> callback) {
        RestAdapter restAdapter = new RestAdapter.Builder()
            .setLogLevel(RestAdapter.LogLevel.FULL)
            .setEndpoint(MPESO_URL)
            .build();

        BalanceServiceInterface service = restAdapter.create(BalanceServiceInterface.class);
        service.getBalanceAsync("1", card.getCard(), callback);
    }

}
