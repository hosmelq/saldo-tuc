package com.socialimprover.saldotuc;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

public class MpesoService {

    protected BalanceServiceInterface mRestAdapter;

    private static final String MPESO_URL = "https://mpeso.net/";

    public MpesoService() {
        RestAdapter restAdapter = new RestAdapter.Builder()
            // .setLogLevel(RestAdapter.LogLevel.FULL)
            .setEndpoint(MPESO_URL)
            .build();

        mRestAdapter = restAdapter.create(BalanceServiceInterface.class);
    }

    public interface BalanceServiceInterface {

        @FormUrlEncoded
        @POST("/datos/consulta.php")
        public MpesoBalance getBalanceSync(
            @Field("_funcion") String _funcion,
            @Field("_terminal") String _terminal
        );

        @FormUrlEncoded
        @POST("/datos/consulta.php")
        public void getBalanceAsync(
            @Field("_funcion") String _funcion,
            @Field("_terminal") String _terminal,
            Callback<MpesoBalance> callback
        );

    }

    public MpesoBalance loadBalanceSync(Card card) {
        return mRestAdapter.getBalanceSync("1", card.getNumber());
    }

    public void loadBalance(Card card, Callback<MpesoBalance> callback) {
        mRestAdapter.getBalanceAsync("1", card.getNumber(), callback);
    }

}
