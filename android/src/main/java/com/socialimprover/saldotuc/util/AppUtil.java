package com.socialimprover.saldotuc.util;

import android.text.Html;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.socialimprover.saldotuc.util.LogUtils.makeLogTag;

public class AppUtil {
    public static final String TAG = makeLogTag("AppUtil");

    public static String parseBalance(String balance) {
        Pattern pattern = Pattern.compile("[0-9]+(?:\\.[0-9]*)?");
        Matcher matcher = pattern.matcher(Html.fromHtml(balance).toString());

        if (matcher.find()) {
            return matcher.group(0);
        } else {
            return null;
        }
    }
}
