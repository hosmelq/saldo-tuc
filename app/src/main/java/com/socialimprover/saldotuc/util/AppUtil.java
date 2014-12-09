package com.socialimprover.saldotuc.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.Html;
import android.util.DisplayMetrics;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AppUtil {

    public static final String TAG = AppUtil.class.getSimpleName();

    public static String formatCard(String number) {
        return number.substring(0, 4) + "-" + number.substring(4, 8);
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param context Context to get resources and device specific display metrics
     * @param px A value in px (pixels) unit. Which we need to convert into db
     * @return A float value to represent dp equivalent to px value
     */
    public static float pxToDp(Context context, int px) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();

        return px / (metrics.densityDpi / 160f);
    }

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param context Context to get resources and device specific display metrics
     * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static float dpToPx(Context context, int dp) {
        Resources resources2 = context.getResources();
        DisplayMetrics metrics = resources2.getDisplayMetrics();

        return dp * (metrics.densityDpi / 160f);
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        boolean isAvailable = false;

        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }

        return isAvailable;
    }

    public static String parseBalance(String balance) {
        Pattern pattern = Pattern.compile("[0-9]+(?:\\.[0-9]*)?");
        Matcher matcher = pattern.matcher(Html.fromHtml(balance).toString());

        if (matcher.find()) {
            return matcher.group(0);
        } else {
            return null;
        }
    }

    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static void showDialog(Context context, String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok, null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
