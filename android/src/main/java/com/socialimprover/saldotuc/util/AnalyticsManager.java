package com.socialimprover.saldotuc.util;

import android.content.Context;

import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.parse.ParseInstallation;

import org.json.JSONException;
import org.json.JSONObject;

import static com.socialimprover.saldotuc.util.LogUtils.LOGD;
import static com.socialimprover.saldotuc.util.LogUtils.LOGE;
import static com.socialimprover.saldotuc.util.LogUtils.makeLogTag;

public class AnalyticsManager {
    private final static String TAG = makeLogTag("AnalyticsManager");
    public static final String MIXPANEL_TOKEN = "1852b173c617cc4814d32633500e008d";
    private static Context mContext;
    private static MixpanelAPI mTracker;

    public static synchronized void initializeAnalyticsTracker(Context context) {
        mContext = context;

        if (mTracker == null) {
            // Initialize the library with your
            mTracker = MixpanelAPI.getInstance(context, MIXPANEL_TOKEN);
            ParseInstallation installation = ParseInstallation.getCurrentInstallation();

            if (installation != null && installation.getInstallationId() != null) {
                mTracker.identify(installation.getInstallationId());
            }
        }
    }

    private static boolean canSend() {
        return mContext != null && mTracker != null;
    }

    public static MixpanelAPI getTracker() {
        return mTracker;
    }

    public static synchronized void setTracker(MixpanelAPI tracker) {
        mTracker = tracker;
    }

    public static void sendScreenView(String screenName) {
        if (canSend()) {
            try {
                JSONObject props = new JSONObject();
                props.put("Screen", screenName);

                mTracker.track("Screen View", props);

                LOGD(TAG, "Screen View recorded: " + screenName);
            } catch (JSONException e) {
                LOGE(TAG, e.getMessage());
            }
        } else {
            LOGD(TAG, "Screen View NOT recorded (analytics disabled or not ready).");
        }
    }

    public static void sendEvent(String category, String action, String label) {
        if (canSend()) {
            try {
                JSONObject props = new JSONObject();
                props.put("Category", category);
                props.put("Label", label);

                mTracker.track(action, props);

                LOGD(TAG, "Event recorded:");
                LOGD(TAG, "\tCategory: " + category);
                LOGD(TAG, "\tAction: " + action);
                LOGD(TAG, "\tLabel: " + label);
            } catch (JSONException e) {
                LOGE(TAG, e.getMessage());
            }
        } else {
            LOGD(TAG, "Analytics event ignored (analytics disabled or not ready).");
        }
    }

    public static void timeEvent(String event) {
        if (canSend()) {
            mTracker.timeEvent(event);
        }
    }
}
