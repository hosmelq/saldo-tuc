package com.socialimprover.saldotuc;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.parse.Parse;
import com.parse.ParseConfig;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.socialimprover.saldotuc.model.Card;
import com.socialimprover.saldotuc.util.SyncHelper;

import java.util.List;

import io.fabric.sdk.android.Fabric;
import rx.Observable;

import static com.socialimprover.saldotuc.util.LogUtils.makeLogTag;

public class SaldoTuc extends Application {
    public static final String TAG = makeLogTag("SaldoTuc");

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        Parse.enableLocalDatastore(this);
        ParseObject.registerSubclass(Card.class);

        Parse.initialize(
            new Parse.Configuration.Builder(getApplicationContext())
                .enableLocalDataStore()
                .applicationId("NeTnXlLHuQj0BQ7iTzfnhxytwjAKUpYnoNmaeNS0")
                .clientKey("GnH9qJsASiOCLs6g57I4Ts7kWI95F02Rzry4GchL")
                .server("https://parse.saldotuc.com/api/")
                .build()
        );

        ParseInstallation.getCurrentInstallation().saveInBackground();
        ParseConfig.getInBackground();
        checkIfNeedSyncNotificationSettings();
    }

    public void updateInstallationChannels(List<String> channels) {
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        installation.put("channels", channels);
        installation.saveInBackground();
    }

    private void checkIfNeedSyncNotificationSettings() {
        if (SyncHelper.isOnline(this)) {
            Card.getQuery()
                .fromLocalDatastore()
                .findInBackground((cards, e) -> {
                    if (e == null) {
                        Observable.from(cards)
                            .filter(Card::getNotifications)
                            .map(Card::getChannelName)
                            .toList()
                            .subscribe(this::updateInstallationChannels);
                    }
                });
        }
    }
}
