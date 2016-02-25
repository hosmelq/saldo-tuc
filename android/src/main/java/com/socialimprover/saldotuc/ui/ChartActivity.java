package com.socialimprover.saldotuc.ui;

import android.animation.PropertyValuesHolder;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;

import com.db.chart.Tools;
import com.db.chart.model.LineSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.ChartView;
import com.db.chart.view.LineChartView;
import com.db.chart.view.Tooltip;
import com.socialimprover.saldotuc.Config;
import com.socialimprover.saldotuc.R;
import com.socialimprover.saldotuc.api.MpesoService;
import com.socialimprover.saldotuc.api.SaldoTucContract.CardColumns;
import com.socialimprover.saldotuc.api.SaldoTucService;
import com.socialimprover.saldotuc.api.ServiceFactory;
import com.socialimprover.saldotuc.api.model.Balance;
import com.socialimprover.saldotuc.exceptions.InvalidCardException;
import com.socialimprover.saldotuc.model.Card;
import com.socialimprover.saldotuc.util.AnalyticsManager;
import com.socialimprover.saldotuc.util.AppUtil;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit2.adapter.rxjava.HttpException;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.socialimprover.saldotuc.util.LogUtils.makeLogTag;

public class ChartActivity extends BaseActivity {
    public static final String TAG = makeLogTag("ChartActivity");
    private final static String SCREEN_LABEL = "Chart";
    private Card mCard;
    private String[] mChartLabels;
    private float[] mBalanceValues;
    private float[] mSpendingValues;

    @Bind(R.id.linechart) LineChartView mChart;
    @Bind(R.id.progressBar) ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        ButterKnife.bind(this);

        final Toolbar toolbar = getActionBarToolbar();
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(view -> finish());

        String cardUuId = Card.getIdFromUri(getIntent().getData());
        Card.getQuery()
            .whereEqualTo(CardColumns.COLUMN_UUID, cardUuId)
            .fromLocalDatastore()
            .getFirstInBackground((card, e) -> {
                if (e == null) {
                    mCard = card;
                    toolbar.setTitle(getString(R.string.title_activity_chart, card.getName(), card.getNumberFormatted()));
                    getBalances(card);
                } else {
                    finish();
                }
            });
    }

    private void hideLoading() {
        mProgressBar.setVisibility(View.GONE);
        mProgressBar.setIndeterminate(false);
    }

    private void showLoading() {
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressBar.setIndeterminate(true);
    }

    private void getBalances(Card card) {
        MpesoService mpesoService = ServiceFactory.createRetrofitService(MpesoService.class, MpesoService.SERVICE_ENDPOINT);
        SaldoTucService saldoTucService = ServiceFactory.createRetrofitService(SaldoTucService.class, SaldoTucService.SERVICE_ENDPOINT);

        showLoading();
        AnalyticsManager.timeEvent(Config.MIXPANEL_REQUEST_BALANCE_EVENT);
        mpesoService.getBalance("1", card.getNumber())
            .flatMap(cardResponse -> {
                String balance = AppUtil.parseBalance(cardResponse.Mensaje);

                if (balance == null) {
                    return Observable.error(new InvalidCardException("Not a TUC card number or inactive"));
                }

                /* [ANALYTICS:EVENT]
                * TRIGGER:  Request card's balance.
                * CATEGORY: 'Chart'
                * ACTION:   'Request Balance'
                * LABEL:    card balance
                * [/ANALYTICS]
                */
                AnalyticsManager.sendEvent(SCREEN_LABEL, Config.MIXPANEL_REQUEST_BALANCE_EVENT, balance);
                mCard.setBalance(balance);
                mCard.pinInBackground();

                return saldoTucService.storeBalance(card.getNumber(), balance);
            })
            .flatMap(balance -> saldoTucService.balances(card.getNumber()))
            .subscribeOn(Schedulers.newThread())
            .unsubscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(balances -> {
                hideLoading();
                buildChartValues(balances);
                produceThree(mChart);
            }, throwable -> {
                hideLoading();
                Intent intent = new Intent();

                if (throwable instanceof InvalidCardException) {
                    intent.putExtra(CardsActivity.NOT_A_VALID_CARD, true);
                } else if (throwable instanceof HttpException) {
                    HttpException exception = (HttpException) throwable;
                }

                setResult(RESULT_CANCELED, intent);
                finish();
            });
    }

    private void buildChartValues(List<Balance> balances) {
        int balanceSize = balances.size();
        SimpleDateFormat dateFormat = new SimpleDateFormat("d MMM", Locale.getDefault());
        mChartLabels = new String[balanceSize];
        mBalanceValues = new float[balanceSize];
        mSpendingValues = new float[balanceSize];

        for (int i = 0; i < balanceSize; i++) {
            Balance balance = balances.get(i);
            mChartLabels[i] = dateFormat.format(balance.created);
            mBalanceValues[i] = balance.balance;
            mSpendingValues[i] = balance.spending;
        }
    }

    private void produceThree(LineChartView chart) {
        Tooltip tip = new Tooltip(this, R.layout.linechart_three_tooltip, R.id.value);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            tip.setEnterAnimation(PropertyValuesHolder.ofFloat(View.ALPHA, 1),
                PropertyValuesHolder.ofFloat(View.SCALE_X, 1f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f));

            tip.setExitAnimation(PropertyValuesHolder.ofFloat(View.ALPHA, 0),
                PropertyValuesHolder.ofFloat(View.SCALE_X, 0f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 0f));
        }

        chart.setTooltips(tip);

        LineSet dataset = new LineSet(mChartLabels, mBalanceValues);
        dataset.setColor(Color.parseColor("#FF365EAF"))
            .setDotsStrokeThickness(Tools.fromDpToPx(2))
            .setDotsStrokeColor(Color.parseColor("#FF365EAF"))
            .setDotsColor(Color.parseColor("#eef1f6"));
        chart.addData(dataset);

        dataset = new LineSet(mChartLabels, mSpendingValues);
        dataset.setColor(Color.parseColor("#FFA03436"))
            .setDotsStrokeThickness(Tools.fromDpToPx(2))
            .setDotsStrokeColor(Color.parseColor("#FFA03436"))
            .setDotsColor(Color.parseColor("#eef1f6"));
        chart.addData(dataset);

        Paint gridPaint = new Paint();
        gridPaint.setColor(Color.parseColor("#308E9196"));
        gridPaint.setAntiAlias(true);
        gridPaint.setStrokeWidth(Tools.fromDpToPx(1f));

        chart.setBorderSpacing(1)
            .setXLabels(AxisController.LabelPosition.OUTSIDE)
            .setYLabels(AxisController.LabelPosition.OUTSIDE)
            .setLabelsColor(Color.parseColor("#FF8E9196"))
            .setXAxis(false)
            .setYAxis(false)
            .setStep(5)
            .setGrid(ChartView.GridType.VERTICAL, gridPaint);

        chart.show();
    }
}
