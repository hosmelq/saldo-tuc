package com.socialimprover.saldotuc;

import android.app.Application;
import android.content.Context;

import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.socialimprover.saldotuc.provider.CardDataSource;

public class SaldoTucApplication extends Application {

    public static final String TAG = SaldoTucApplication.class.getSimpleName();

    private static CardDataSource mDataSource;

    @Override
    public void onCreate() {
        super.onCreate();
        mDataSource = new CardDataSource(this);
        mDataSource.open();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        mDataSource.close();
        mDataSource = null;
    }

    public static CardDataSource getDatabaseHelper() {
        return mDataSource;
    }

    public static MixpanelAPI getMixpanelInstance(Context context) {
        return MixpanelAPI.getInstance(context, "1852b173c617cc4814d32633500e008d");
    }

}
