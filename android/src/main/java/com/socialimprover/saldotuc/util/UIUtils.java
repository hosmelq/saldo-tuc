package com.socialimprover.saldotuc.util;

import static com.socialimprover.saldotuc.util.LogUtils.makeLogTag;

public class UIUtils {
    public static final String TAG = makeLogTag(UIUtils.class);

    public static String formatCardNumber(String number) {
        if (number == null || number.isEmpty()) {
            return "";
        }

        return number.substring(0, 4) + "-" + number.substring(4, 8);
    }
}
