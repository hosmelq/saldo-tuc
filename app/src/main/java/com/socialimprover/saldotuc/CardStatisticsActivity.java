package com.socialimprover.saldotuc;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import com.androidplot.ui.XLayoutStyle;
import com.androidplot.ui.YLayoutStyle;
import com.androidplot.xy.*;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.socialimprover.saldotuc.app.R;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import java.text.*;
import java.util.ArrayList;
import java.util.Date;

public class CardStatisticsActivity extends ActionBarActivity {

    public static final String TAG = CardStatisticsActivity.class.getSimpleName();

    protected CardDataSource mDataSource;
    protected MixpanelAPI mMixpanel;

    protected Card mCard;
    protected XYPlot mChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_card_statistics);

        mDataSource = SaldoTucApplication.getDatabaseHelper();
        mMixpanel = SaldoTucApplication.getMixpanelInstance(this);

        mCard = (Card) getIntent().getSerializableExtra("card");
        mChart = (XYPlot) findViewById(R.id.mySimpleXYPlot);

        getSupportActionBar().setTitle(getString(R.string.title_activity_card_statistics) + " - " + AppUtil.formatCard(mCard.getNumber()));

        setSupportProgressBarIndeterminateVisibility(true);

        MpesoService service = new MpesoService();
        service.loadBalance(mCard, mBalanceCallback);
    }

    @Override
    protected void onDestroy() {
        mMixpanel.flush();
        super.onDestroy();
    }

    protected Callback<MpesoBalance> mBalanceCallback = new Callback<MpesoBalance>() {
        @Override
        public void success(MpesoBalance mpesoBalance, Response response) {
            String balance = AppUtil.parseBalance(mpesoBalance.Mensaje);

            if ( ! mpesoBalance.Error && balance != null) {
                mCard.setBalance(balance);
                mDataSource.update(mCard);

                trackBalance(mCard.getBalance(), mCard.getNumber());

                SaldoTucService service = new SaldoTucService();
                service.getBalances(mCard, mStatisticsBalanceCallback);
            } else {
                AppUtil.showToast(CardStatisticsActivity.this, getString(R.string.card_invalid));
                removeProgressBar();
                finish();
            }
        }

        @Override
        public void failure(RetrofitError error) {
            removeProgressBar();
            if (error.isNetworkError()) {
                AppUtil.showToast(CardStatisticsActivity.this, getString(R.string.network_error));
            }
            Log.e(TAG, "Error: " + error.getMessage());
            finish();
        }
    };

    protected Callback<Records> mStatisticsBalanceCallback = new Callback<Records>() {
        @Override
        public void success(Records records, Response response) {
            removeProgressBar();

            if (records.data.size() <= 0) {
                AppUtil.showToast(CardStatisticsActivity.this, getString(R.string.no_statistics_data));
                finish();
            } else {
                mChart.setVisibility(View.VISIBLE);
                configChart();
                drawChart(records);
            }
        }

        @Override
        public void failure(RetrofitError error) {
            removeProgressBar();
            Log.e(TAG, "Error: " + error.getMessage());
            finish();
        }
    };

    private void configChart() {
        // set backgrounds
        mChart.setBackgroundColor(Color.TRANSPARENT);
        mChart.getBackgroundPaint().setColor(Color.WHITE);
        mChart.getGraphWidget().getBackgroundPaint().setColor(Color.WHITE);
        mChart.getGraphWidget().getGridBackgroundPaint().setColor(Color.WHITE);

        mChart.getBorderPaint().setColor(Color.TRANSPARENT);

        mChart.getGraphWidget().position(0, XLayoutStyle.ABSOLUTE_FROM_LEFT, 0, YLayoutStyle.ABSOLUTE_FROM_TOP);

//        mChart.setDomainStepValue(5);
//        mChart.setRangeStepValue(5);
        mChart.getGraphWidget().setRangeValueFormat(new DecimalFormat("0"));
        mChart.getGraphWidget().setDomainValueFormat(new DateFormat());
    }

    private void drawChart(Records records) {
        ArrayList<Number> balance = new ArrayList<Number>();
        ArrayList<Number> spending = new ArrayList<Number>();
        ArrayList<Number> dates = new ArrayList<Number>();

        for (int i = 0; i < records.data.size(); i++) {
            balance.add(records.data.get(i).balance);
            spending.add(records.data.get(i).spending);

            DateTime dt = new DateTime(records.data.get(i).created_at);
            dates.add(dt.getMillis());
        }

        if (records.data.size() > 10) {
            // reduce the number of range labels
            // mChart.setTicksPerRangeLabel(3);
            mChart.setTicksPerDomainLabel(4);
        }

        // Turn the above arrays into XYSeries':
        XYSeries series1 = new SimpleXYSeries(dates, balance, "Saldo");
        XYSeries series2 = new SimpleXYSeries(dates, spending, "Gastos");

        // Create a formatter to use for drawing a series using LineAndPointRenderer
        // and configure it from xml:
        LineAndPointFormatter series1Format = new LineAndPointFormatter();
        series1Format.setPointLabelFormatter(new PointLabelFormatter());
        series1Format.configure(getApplicationContext(), R.xml.line_point_formatter_with_plf1);

        // add a new series' to the xyplot:
        mChart.addSeries(series1, series1Format);

        // same as above:
        LineAndPointFormatter series2Format = new LineAndPointFormatter();
        series2Format.setPointLabelFormatter(new PointLabelFormatter());
        series2Format.configure(getApplicationContext(), R.xml.line_point_formatter_with_plf2);

        mChart.addSeries(series2, series2Format);

        mChart.setDomainStep(XYStepMode.SUBDIVIDE, dates.size());
    }

    protected void removeProgressBar() {
        setSupportProgressBarIndeterminateVisibility(false);
    }

    protected void trackBalance(String balance, String number) {
        try {
            JSONObject props = new JSONObject();
            props.put("balance", Float.parseFloat(balance));
            props.put("tuc", number);

            mMixpanel.identify(number);
            mMixpanel.track("Consulta Saldo", props);
        } catch (JSONException e) {}
    }

    private class DateFormat extends Format {
        private SimpleDateFormat dateFormat = new SimpleDateFormat("d. MMM");

        @Override
        public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
            Date date = new Date(((Number) obj).longValue());

            return dateFormat.format(date, toAppendTo, pos);
        }

        @Override
        public Object parseObject(String source, ParsePosition pos) {
            return null;

        }
    }

}
