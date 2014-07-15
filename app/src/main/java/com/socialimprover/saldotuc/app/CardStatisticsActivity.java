package com.socialimprover.saldotuc.app;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.androidplot.ui.XLayoutStyle;
import com.androidplot.ui.YLayoutStyle;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PointLabelFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.XYStepMode;

import org.joda.time.DateTime;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class CardStatisticsActivity extends ActionBarActivity {

    public static final String TAG = CardAddActivity.class.getSimpleName();

    protected Card mCard;
    protected Records mRecords;
    protected XYPlot mChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_statistics);

        mCard = (Card) getIntent().getSerializableExtra("card");
        mRecords = (Records) getIntent().getSerializableExtra("records");
        mChart = (XYPlot) findViewById(R.id.mySimpleXYPlot);

        configChart();

        drawChart(mRecords);
    }

    private void configChart() {
        // set backgrounds
        mChart.setBackgroundColor(Color.TRANSPARENT);
        mChart.getBackgroundPaint().setColor(Color.WHITE);
        mChart.getGraphWidget().getBackgroundPaint().setColor(Color.WHITE);
        mChart.getGraphWidget().getGridBackgroundPaint().setColor(Color.WHITE);

        mChart.getBorderPaint().setColor(Color.TRANSPARENT);

        mChart.getGraphWidget().position(0, XLayoutStyle.ABSOLUTE_FROM_LEFT, 0, YLayoutStyle.ABSOLUTE_FROM_TOP);

//        mChart.setDomainStepValue(5);
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

        // reduce the number of range labels
//        mChart.setTicksPerRangeLabel(3);
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
