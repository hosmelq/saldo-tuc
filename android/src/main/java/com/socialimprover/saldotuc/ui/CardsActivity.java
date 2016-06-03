package com.socialimprover.saldotuc.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.socialimprover.saldotuc.Config;
import com.socialimprover.saldotuc.R;
import com.socialimprover.saldotuc.api.MpesoService;
import com.socialimprover.saldotuc.api.SaldoTucContract.CardColumns;
import com.socialimprover.saldotuc.api.SaldoTucService;
import com.socialimprover.saldotuc.api.ServiceFactory;
import com.socialimprover.saldotuc.model.Card;
import com.socialimprover.saldotuc.util.AnalyticsManager;
import com.socialimprover.saldotuc.util.SyncHelper;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.socialimprover.saldotuc.util.LogUtils.makeLogTag;

public class CardsActivity extends BaseActivity implements CardAdapter.Callbacks {
    public static final String TAG = makeLogTag("CardsActivity");
    private final static String SCREEN_LABEL = "Cards";
    public static final int ADD_CARD_REQUEST = 101;
    public static final String WILL_SUBSCRIBE_WHEN_ONLINE = "WILL_SUBSCRIBE_WHEN_ONLINE";
    public static final int UPDATE_CARD_REQUEST = 102;
    public static final String WILL_UNSUBSCRIBE_WHEN_ONLINE = "WILL_UNSUBSCRIBE_WHEN_ONLINE";
    public static final int SHOW_CHART_REQUEST = 103;
    public static final String NOT_A_VALID_CARD = "NOT_A_VALID_CARD";
    public static final String MPESO_CONNECTION_ERROR = "MPESO_CONNECTION_ERROR";
    private CardAdapter mCardsAdapter;
    private Handler mHandler = new Handler();

    @Bind(R.id.swipeContainer) SwipeRefreshLayout mSwipeContainer;
    @Bind(R.id.recyclerView) RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cards);
        ButterKnife.bind(this);

        final Toolbar toolbar = getActionBarToolbar();
        mHandler.post(() -> toolbar.setTitle(R.string.title_activity_cards));

        mSwipeContainer.setOnRefreshListener(() -> {
            if (mCardsAdapter.getItemCount() <= 0) {
                removeSwipe();
            } else {
                if (!SyncHelper.isOnline(CardsActivity.this)) {
                    removeSwipe();
                    quickSnackbar(getString(R.string.no_connection));
                } else {
                    refreshCards();
                }
            }
        });

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        RecyclerView.ItemAnimator animator = mRecyclerView.getItemAnimator();

        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateList();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ADD_CARD_REQUEST) {
            onAddCardResult(resultCode, data);
        }

        if (requestCode == UPDATE_CARD_REQUEST) {
            onUpdateCardResult(resultCode, data);
        }

        if (requestCode == SHOW_CHART_REQUEST) {
            onShowChartResult(resultCode, data);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.cards, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_refresh_cards:
                mSwipeContainer.setRefreshing(true);
                refreshCards();
                return true;

            case R.id.action_mpeso_agencies:
                Intent intent = new Intent(this, AgenciesActivity.class);
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCardEdit(Card card) {
        Intent intent = new Intent(this, CardUpdateActivity.class);
        intent.setData(card.getUri());
        startActivityForResult(intent, UPDATE_CARD_REQUEST);
    }

    @Override
    public void onShowChart(Card card) {
        if (SyncHelper.isOnline(this)) {
            Intent intent = new Intent(this, ChartActivity.class);
            intent.setData(card.getUri());
            startActivityForResult(intent, SHOW_CHART_REQUEST);
        } else {
            quickSnackbar(getString(R.string.no_connection));
        }
    }

    @OnClick(R.id.addCardButton)
    protected void addCard() {
        Intent intent = new Intent(this, CardAddActivity.class);
        startActivityForResult(intent, ADD_CARD_REQUEST);
    }

    private void updateList() {
        mCardsAdapter = new CardAdapter(this);
        mRecyclerView.setAdapter(mCardsAdapter);

        Card.getQuery()
            .orderByAscending(CardColumns.COLUMN_NAME_LOWERCASE)
            .fromLocalDatastore()
            .findInBackground(mCardsAdapter);
    }

    private void removeSwipe() {
        if (mSwipeContainer.isRefreshing()) {
            mSwipeContainer.setRefreshing(false);
        }
    }

    private void refreshCards() {
        MpesoService mpesoService = ServiceFactory.createRetrofitService(MpesoService.class, MpesoService.SERVICE_ENDPOINT);
        SaldoTucService saldoTucService = ServiceFactory.createRetrofitService(SaldoTucService.class, SaldoTucService.SERVICE_ENDPOINT);

        Card.getQuery()
            .fromLocalDatastore()
            .findInBackground((objects, exception) -> Observable.from(objects)
                .flatMap(card -> mpesoService.getBalance(card.getNumber()).flatMap(mpesoCard -> {
                    String balance = String.valueOf(mpesoCard.balance);

                    /* [ANALYTICS:EVENT]
                    * TRIGGER:  Request card's balance.
                    * CATEGORY: 'Cards'
                    * ACTION:   'Request Balance'
                    * LABEL:    card balance
                    * [/ANALYTICS]
                    */
                    AnalyticsManager.sendEvent(SCREEN_LABEL, Config.MIXPANEL_REQUEST_BALANCE_EVENT, balance);
                    card.setBalance(balance);
                    card.pinInBackground();

                    return saldoTucService.storeBalance(card.getNumber(), balance);
                }))
                .subscribeOn(Schedulers.newThread())
                .unsubscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(balance -> mCardsAdapter.notifyDataSetChanged(), e -> removeSwipe(), this::removeSwipe));
    }

    private void onAddCardResult(int resultCode, Intent data) {
        if (resultCode == RESULT_OK && data != null) {
            if (data.hasExtra(WILL_SUBSCRIBE_WHEN_ONLINE)) {
                quickSnackbar(getString(R.string.will_subscribe_when_online));
            }
        }
    }

    private void onUpdateCardResult(int resultCode, Intent data) {
        if (resultCode == RESULT_OK && data != null) {
            if (data.hasExtra("card")) {
                mCardsAdapter.notifyDataSetChanged();
            }

            if (data.hasExtra(WILL_SUBSCRIBE_WHEN_ONLINE)) {
                quickSnackbar(getString(R.string.will_subscribe_when_online));
            } else if (data.hasExtra(WILL_UNSUBSCRIBE_WHEN_ONLINE)) {
                quickSnackbar(getString(R.string.will_unsubscribe_when_online));
            }
        }
    }

    private void onShowChartResult(int resultCode, Intent data) {
        if (resultCode == RESULT_CANCELED && data != null) {
            if (data.hasExtra(NOT_A_VALID_CARD)) {
                quickSnackbar(getString(R.string.card_invalid_error));
            } else if (data.hasExtra(MPESO_CONNECTION_ERROR)) {
                quickSnackbar(getString(R.string.mpeso_connection_error));
            }
        }
    }

    public void quickSnackbar(String text) {
        //noinspection ResourceType
        Snackbar.make(findViewById(R.id.content), text, Config.SNACKBAR_LONG_DURATION_MS).show();
    }
}
