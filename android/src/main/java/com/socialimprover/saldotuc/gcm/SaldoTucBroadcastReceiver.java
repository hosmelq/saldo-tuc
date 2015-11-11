package com.socialimprover.saldotuc.gcm;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;

import com.parse.ParsePushBroadcastReceiver;
import com.socialimprover.saldotuc.R;
import com.socialimprover.saldotuc.api.SaldoTucContract.CardColumns;
import com.socialimprover.saldotuc.api.SaldoTucService;
import com.socialimprover.saldotuc.api.ServiceFactory;
import com.socialimprover.saldotuc.model.Card;
import com.socialimprover.saldotuc.util.UIUtils;

import org.json.JSONException;
import org.json.JSONObject;

import rx.schedulers.Schedulers;

import static com.socialimprover.saldotuc.util.LogUtils.LOGD;
import static com.socialimprover.saldotuc.util.LogUtils.makeLogTag;

public class SaldoTucBroadcastReceiver extends ParsePushBroadcastReceiver {
    public static final String TAG = makeLogTag("SaldoTucBroadcastReceiver");
    public static final String KEY_CARD_NUMBER = "card_number";
    public static final String KEY_CARD_BALANCE = "card_balance";
    public static final String KEY_NOTIFICATION_ID = "notification_id";

    @Override
    protected int getSmallIconId(Context context, Intent intent) {
        return R.mipmap.ic_push_icon;
    }

    @Override
    protected Notification getNotification(Context context, Intent intent) {
        try {
            JSONObject pushData = new JSONObject(intent.getStringExtra(KEY_PUSH_DATA));

            if (pushData.has(KEY_CARD_NUMBER) && pushData.has(KEY_CARD_BALANCE)) {
                String number = pushData.optString(KEY_CARD_NUMBER);
                String balance = pushData.optString(KEY_CARD_BALANCE);
                int notificationId = pushData.optInt(KEY_NOTIFICATION_ID);

                pushData.put("alert", context.getString(R.string.push_title, UIUtils.formatCardNumber(number), balance));
                pushData.put("title", context.getString(R.string.push_alert));
                intent.putExtra(KEY_PUSH_DATA, pushData.toString());

                updateCardBalance(number, balance);
                markNotificationAsReceived(number, notificationId);
            }
        } catch (JSONException e) {
            LOGD(TAG, "Unexpected JSONException when receiving push data: ", e);
            return null;
        }

        return super.getNotification(context, intent);
    }

    private void updateCardBalance(String number, String balance) {
        Card.getQuery()
            .fromLocalDatastore()
            .whereEqualTo(CardColumns.COLUMN_NUMBER, number)
            .getFirstInBackground((card, e) -> {
                if (e == null) {
                    card.setBalance(balance);
                    card.pinInBackground();
                }
            });
    }

    private void markNotificationAsReceived(String number, int id) {
        ServiceFactory.createRetrofitService(SaldoTucService.class, SaldoTucService.SERVICE_ENDPOINT)
            .notificationReceived(number, id)
            .subscribeOn(Schedulers.newThread())
            .unsubscribeOn(Schedulers.newThread())
            .observeOn(Schedulers.newThread())
            .subscribe(notification -> {
            }, throwable -> {
            });
    }
}
