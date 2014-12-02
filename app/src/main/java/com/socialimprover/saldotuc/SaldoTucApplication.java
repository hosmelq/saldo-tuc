package com.socialimprover.saldotuc;

import android.app.Application;
import android.content.Context;

import com.mixpanel.android.mpmetrics.MixpanelAPI;

public class SaldoTucApplication extends Application {

    public static final String TAG = SaldoTucApplication.class.getSimpleName();

    public static MixpanelAPI getMixpanelInstance(Context context) {
        return MixpanelAPI.getInstance(context, "1852b173c617cc4814d32633500e008d");
    }

}
