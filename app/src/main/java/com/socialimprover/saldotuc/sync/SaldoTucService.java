package com.socialimprover.saldotuc.sync;

import com.socialimprover.saldotuc.models.Agency;
import com.socialimprover.saldotuc.models.Card;
import com.socialimprover.saldotuc.models.District;
import com.socialimprover.saldotuc.models.Records;

import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;

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

        @POST("/cards")
        public void storeCardAsync(
            @Body Card card,
            Callback<Card> callback
        );

        @PUT("/cards/{id}")
        public void updateCardAsync(
            @Path("id") Integer id,
            @Body Card card,
            Callback<Card> callback
        );

        @DELETE("/cards/{id}")
        public void deleteCardAsync(
            @Path("id") Integer id,
            @Query("phone") String phone,
            Callback<Response> callback
        );

        @FormUrlEncoded
        @POST("/cards/verify")
        public void verifyCardAsync(
            @Field("phone") String phone,
            @Field("phone_old") String phoneOld,
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
            @Field("number") String number,
            @Field("balance") String card,
            Callback<Records> callback
        );

        @GET("/districts")
        public void getDistrictsAsync(
            Callback<List<District>> callback
        );

        @GET("/districts/{id}/agencies")
        public void getAgenciesByDistrictAsync(
            @Path("id") String id,
            Callback<List<Agency>> callback
        );

    }

    public void storeCard(Card card, Callback<Card> callback) {
        mRestAdapter.storeCardAsync(card, callback);
    }

    public void updateCard(Card card, Callback<Card> callback) {
        mRestAdapter.updateCardAsync(card.getId(), card, callback);
    }

    public void deleteCard(Card card, Callback<Response> callback) {
        mRestAdapter.deleteCardAsync(card.getId(), card.getPhone(), callback);
    }

    public void verifyCard(String phone, String phoneOld, String code, Callback<Card> callback) {
        mRestAdapter.verifyCardAsync(phone, phoneOld, code, callback);
    }

    public void storeBalance(Card card, Callback<Card> callback) {
        mRestAdapter.storeBalanceAsync(card, callback);
    }

    public void getBalances(Card card, Callback<Records> callback) {
        mRestAdapter.getBalancesAsync(card.getNumber(), card.getBalance(), callback);
    }

    public void getDistricts(Callback<List<District>> callback) {
        mRestAdapter.getDistrictsAsync(callback);
    }

    public void getAgenciesByDistrict(District district, Callback<List<Agency>> callback) {
        mRestAdapter.getAgenciesByDistrictAsync(district.getId(), callback);
    }

}
