package com.socialimprover.saldotuc;

import android.app.Application;
import android.content.Context;

import com.mixpanel.android.mpmetrics.MixpanelAPI;

public class SaldoTucApplication extends Application {

    public static MixpanelAPI getMixpanelInstance(Context context) {
        return MixpanelAPI.getInstance(context, "e2264dc0ad33218e0264b4bba2c57951");
    }

}
