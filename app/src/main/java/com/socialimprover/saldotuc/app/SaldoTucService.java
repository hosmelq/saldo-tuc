package com.socialimprover.saldotuc.app;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.http.Body;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

public class SaldoTucService {

    protected SaldoTucServiceInterface mRestAdapter;

    protected static final String API_END_POINT = "http://saldotuc.hosmelquintana.com/api/v1/";

    public SaldoTucService() {
        RestAdapter restAdapter = new RestAdapter.Builder()
            .setLogLevel(RestAdapter.LogLevel.FULL)
            .setEndpoint(API_END_POINT)
            .build();

        mRestAdapter = restAdapter.create(SaldoTucServiceInterface.class);
    }

    public interface SaldoTucServiceInterface {

        @POST("/cards")
        public void storeCardAsync(
            @Body Card card,
            Callback<Card> callback
        );

        @FormUrlEncoded
        @POST("/cards/verify")
        public void verifyCardAsync(
            @Field("phone") String phone,
            @Field("code") String code,
            Callback<Card> callback
        );

        @POST("/records")
        public void storeBalanceAsync(
            @Body Card card,
            Callback<Card> callback
        );

        @FormUrlEncoded
        @POST("/cards/{card}/records")
        public void getBalancesAsync(
            @Field("card") String card,
            Callback<MpesoBalance> callback
        );

    }

    public void storeCard(Card card, Callback<Card> callback) {
        mRestAdapter.storeCardAsync(card, callback);
    }

    public void verifyCard(String phone, String code, Callback<Card> callback) {
        mRestAdapter.verifyCardAsync(phone, code, callback);
    }

    public void storeBalance(Card card, Callback<Card> callback) {
        mRestAdapter.storeBalanceAsync(card, callback);
    }

    public void getBalances(Card card, Callback<MpesoBalance> callback) {
        mRestAdapter.getBalancesAsync(card.getNumber(), callback);
    }

}
