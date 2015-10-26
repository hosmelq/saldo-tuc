package com.socialimprover.saldotuc.model;

import android.net.Uri;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.socialimprover.saldotuc.Config;
import com.socialimprover.saldotuc.api.SaldoTucContract.CardColumns;
import com.socialimprover.saldotuc.util.UIUtils;

import java.util.List;
import java.util.UUID;

@ParseClassName("Card")
public class Card extends ParseObject {

    private static final String uriPath = "agency";

    public String getBalance() {
        return getString(CardColumns.COLUMN_BALANCE);
    }

    public void setBalance(String balance) {
        put(CardColumns.COLUMN_BALANCE, balance);
    }

    public String getName() {
        return getString(CardColumns.COLUMN_NAME);
    }

    public void setName(String name) {
        put(CardColumns.COLUMN_NAME, name);
        put(CardColumns.COLUMN_NAME_LOWERCASE, name.toLowerCase());
    }

    public boolean getNotifications() {
        return getBoolean("notifications");
    }

    public void setNotifications(boolean notifications) {
        put("notifications", notifications);
    }

    public String getNumber() {
        return getString(CardColumns.COLUMN_NUMBER);
    }

    public void setNumber(String number) {
        put(CardColumns.COLUMN_NUMBER, number);
    }

    public String getUuid() {
        return getString(CardColumns.COLUMN_UUID);
    }

    public void setUuid() {
        put(CardColumns.COLUMN_UUID, UUID.randomUUID().toString());
    }

    public String getChannelName() {
        return Config.PARSE_CARD_CHANNEL_PREFIX + getNumber();
    }

    public String getNumberFormatted() {
        return UIUtils.formatCardNumber(getString(CardColumns.COLUMN_NUMBER));
    }

    public static ParseQuery<Card> getQuery() {
        return ParseQuery.getQuery(Card.class);
    }

    public Uri getUri() {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(Config.APP_SCHEME);
        builder.path(String.format("%s/%s", uriPath, getUuid()));

        return builder.build();
    }

    public static String getIdFromUri(Uri uri) {
        List<String> path = uri.getPathSegments();
        if (path.size() != 2 || !uriPath.equals(path.get(0))) {
            throw new RuntimeException("Invalid URI for card: " + uri);
        }

        return path.get(1);
    }
}
