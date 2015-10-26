package com.socialimprover.saldotuc.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import static com.socialimprover.saldotuc.util.LogUtils.makeLogTag;

public class PrefUtils {
    private static final String TAG = makeLogTag("PrefUtils");

    /**
     * Boolean preference that indicates whether we installed the boostrap data or not.
     */
    public static final String PREF_DATA_BOOTSTRAP_DONE = "pref_data_bootstrap_done";

    public static void markDataImportedDone(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_DATA_BOOTSTRAP_DONE, true).apply();
    }

    public static boolean isDataImportedDone(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_DATA_BOOTSTRAP_DONE, false);
    }
}
