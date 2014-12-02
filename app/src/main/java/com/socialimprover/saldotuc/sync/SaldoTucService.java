package com.socialimprover.saldotuc.sync;

import com.socialimprover.saldotuc.models.Agency;
import com.socialimprover.saldotuc.models.Card;
import com.socialimprover.saldotuc.models.Records;

import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.http.Body;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;

public class SaldoTucService {

    protected SaldoTucServiceInterface mRestAdapter;

    protected static final String API_END_POINT = "http://saldotuc.getnerdify.com/api/v1/";

    public SaldoTucService() {
        RestAdapter restAdapter = new RestAdapter.Builder()
            // .setLogLevel(RestAdapter.LogLevel.FULL)
            .setEndpoint(API_END_POINT)
            .build();

        mRestAdapter = restAdapter.create(SaldoTucServiceInterface.class);
    }

    public interface SaldoTucServiceInterface {

        @POST("/records")
        public void storeBalanceAsync(
            @Body Card card,
            Callback<Card> callback
        );

        @FormUrlEncoded
        @POST("/cards/{card}/records")
        public void getBalancesAsync(
            @Field("number") String number,
            @Field("balance") String card,
            Callback<Records> callback
        );

        @GET("/agencies")
        public void getAgenciesAsync(
            Callback<List<Agency>> callback
        );

    }

    public void storeBalance(Card card, Callback<Card> callback) {
        mRestAdapter.storeBalanceAsync(card, callback);
    }

    public void getBalances(Card card, Callback<Records> callback) {
        mRestAdapter.getBalancesAsync(card.getNumber(), card.getBalance(), callback);
    }

    public void getAgencies(Callback<List<Agency>> callback) {
        mRestAdapter.getAgenciesAsync(callback);
    }

}
