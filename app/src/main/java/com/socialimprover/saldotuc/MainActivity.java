package com.socialimprover.saldotuc;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.shamanland.fab.FloatingActionButton;
import com.socialimprover.saldotuc.app.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.fabric.sdk.android.Fabric;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainActivity extends BaseActivity {

    public static final String TAG = MainActivity.class.getSimpleName();

    protected CardDataSource mDataSource;
    protected ListView mListView;
    protected SwipeRefreshLayout mSwipeRefreshLayout;
    protected List<Card> mCards;
    protected Card mCard;
    protected View mCardView;
    protected ImageView mAddBottom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());

        setActionBarTitle(R.string.title_activity_main);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        mDataSource = SaldoTucApplication.getDatabaseHelper();

        mListView = (ListView) findViewById(android.R.id.list);
        mListView.setOnItemClickListener(mOnItemClickListener);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(mOnRefreshListener);
        mSwipeRefreshLayout.setColorScheme(R.color.swipeRefresh1, R.color.swipeRefresh2, R.color.swipeRefresh3, R.color.swipeRefresh4);

        setupAddBottom();
    }

    @Override
    public void onResume() {
        super.onResume();

        updateList();
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_main;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_mpeso_agencies) {
            Intent agenciesIntent = new Intent(this, DistrictsActivity.class);
            startActivity(agenciesIntent);
        }

        return super.onOptionsItemSelected(item);
    }

    protected void setupAddBottom() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mAddBottom = (ImageButton) findViewById(R.id.fab_action_card_add);
            mAddBottom.setBackgroundResource(R.drawable.ripple);
            mAddBottom.setVisibility(ImageButton.VISIBLE);
        } else {
            mAddBottom = (FloatingActionButton) findViewById(R.id.fab);
            mAddBottom.setVisibility(FloatingActionButton.VISIBLE);
        }

        if (mAddBottom != null) {
            mAddBottom.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    addNewCard();
                }
            });
        }
    }

    protected AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            RelativeLayout infoLayout = (RelativeLayout) view.findViewById(R.id.infoLayout);

            Button balanceButton = (Button) view.findViewById(R.id.cardActionBalance);
            Button statisticsButton = (Button) view.findViewById(R.id.cardActionStatistics);
            Button editButton = (Button) view.findViewById(R.id.cardActionEdit);
            Button deleteButton = (Button) view.findViewById(R.id.cardActionDelete);

            balanceButton.setOnClickListener(mActionOnClickListener);
            statisticsButton.setOnClickListener(mActionOnClickListener);
            editButton.setOnClickListener(mActionOnClickListener);
            deleteButton.setOnClickListener(mActionOnClickListener);

            if (infoLayout.getAnimation() == null) {
                hideAllActions();
                showActions(view);
            } else {
                hideActions(view, true);
            }
        }
    };

    protected View.OnClickListener mActionOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int position = mListView.getPositionForView(view);
            int firstVisiblePosition = mListView.getFirstVisiblePosition() - mListView.getHeaderViewsCount();
            int wantedChild = position - firstVisiblePosition;

            mCardView = mListView.getChildAt(wantedChild);
            Card card = mCards.get(position);
            mCard = card;

            switch (view.getId()) {
                case R.id.cardActionBalance: // request balance
                    hideActions(mCardView, true);

                    if ( ! AppUtil.isNetworkAvailable(MainActivity.this)) {
                        AppUtil.showToast(MainActivity.this, getString(R.string.no_connection_message));
                    } else {
                        showProgressBar();

                        MpesoService service = new MpesoService();
                        service.loadBalance(card, mBalanceCallback);
                    }
                    break;
                case R.id.cardActionStatistics: // show statistics
                    hideActions(mCardView, false);

                    if ( ! AppUtil.isNetworkAvailable(MainActivity.this)) {
                        AppUtil.showToast(MainActivity.this, getString(R.string.no_connection_message));
                    } else {
                        Intent statisticsIntent = new Intent(MainActivity.this, CardStatisticsActivity.class);
                        statisticsIntent.putExtra("card", mCard);
                        startActivity(statisticsIntent);
                    }
                    break;
                case R.id.cardActionEdit: // update card
                    hideActions(mCardView, false);

                    Intent updateIntent = new Intent(MainActivity.this, CardUpdateActivity.class);
                    updateIntent.putExtra("card", mCard);
                    startActivity(updateIntent);
                    break;
                case R.id.cardActionDelete: // delete card
                    hideActions(mCardView, true);

                    String number = AppUtil.formatCard(mCard.getNumber());
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("¿ Eliminar la tarjeta " + number + " ?")
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(android.R.string.ok, mDeleteCardDialogListener);

                    AlertDialog dialog = builder.create();
                    dialog.show();
                    break;
            }
        }
    };

    protected Callback<MpesoBalance> mBalanceCallback = new Callback<MpesoBalance>() {
        @Override
        public void success(MpesoBalance mpesoBalance, Response response) {
            String balance = AppUtil.parseBalance(mpesoBalance.Mensaje);

            if ( ! mpesoBalance.Error && balance != null) {
                mCard.setBalance(balance);
                mDataSource.update(mCard);
                ((TextView) mCardView.findViewById(R.id.cardBalance)).setText("C$ " + balance);

                trackBalance(mCard.getBalance(), mCard.getNumber());

                SaldoTucService service = new SaldoTucService();
                service.storeBalance(mCard, new Callback<Card>() {
                    @Override
                    public void success(Card card, Response response) {
                        hideProgressBar();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        hideProgressBar();
                    }
                });
            } else {
                AppUtil.showToast(MainActivity.this, String.format(getString(R.string.card_invalid), AppUtil.formatCard(mCard.getNumber())));
                hideProgressBar();
            }
        }

        @Override
        public void failure(RetrofitError error) {
            hideProgressBar();
            if (error.isNetworkError()) {
                AppUtil.showToast(MainActivity.this, getString(R.string.network_error));
            }
        }
    };

    protected DialogInterface.OnClickListener mDeleteCardDialogListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            if (mCard.getPhone() != null) {
                if (AppUtil.isNetworkAvailable(MainActivity.this)) {
                    setSupportProgressBarIndeterminateVisibility(true);

                    SaldoTucService service = new SaldoTucService();

                    service.deleteCard(mCard, new Callback<Response>() {
                        @Override
                        public void success(Response result, Response response) {
                            hideProgressBar();
                            deleteCard(mCard);
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            hideProgressBar();
                        }
                    });
                } else {
                    AppUtil.showDialog(MainActivity.this, getString(R.string.error_title), "Necesitas conexión a internet para eliminar esta tarjeta.");
                }
            } else {
                deleteCard(mCard);
            }
        }
    };

    protected SwipeRefreshLayout.OnRefreshListener mOnRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            if (mCards.isEmpty() && mSwipeRefreshLayout.isRefreshing()) {
                mSwipeRefreshLayout.setRefreshing(false);
            } else {
                if ( ! AppUtil.isNetworkAvailable(MainActivity.this)) {
                    mSwipeRefreshLayout.setRefreshing(false);
                    AppUtil.showToast(MainActivity.this, getString(R.string.no_connection_message));
                } else {
                    GetMpesoBalances getMpesoBalance = new GetMpesoBalances(mCards);
                    getMpesoBalance.execute();
                }
            }
        }
    };

    protected List<Card> getCards() {
        List<Card> cards = new ArrayList<Card>();
        Cursor cursor = mDataSource.all();

        cursor.moveToFirst();

        while( ! cursor.isAfterLast() ) {
            Card card = new Card();

            card.setId(cursor.getInt(0));
            card.setName(cursor.getString(1));
            card.setNumber(cursor.getString(2));
            card.setPhone(cursor.getString(3));
            card.setHour(cursor.getString(4));
            card.setAmpm(cursor.getString(5));

            if ( ! cursor.isNull(6)) {
                card.setBalance(cursor.getString(6));
            }

            cards.add(card);

            cursor.moveToNext();
        }

        return cards;
    }

    protected void updateList() {
        List<Card> cards = getCards();

        mCards = cards;

        if (mListView.getAdapter() == null) {
            CardAdapter adapter = new CardAdapter(this, cards);
            mListView.setEmptyView(findViewById(android.R.id.empty));
            mListView.setAdapter(adapter);
        } else {
            ( (CardAdapter) mListView.getAdapter()).refill(cards);
        }
    }

    protected void addNewCard() {
        Intent intent = new Intent(this, CardAddActivity.class);
        startActivity(intent);
    }

    protected void showActions(View view) {
        LinearLayout actionsLayout = (LinearLayout) view.findViewById(R.id.actionsLayout);
        RelativeLayout infoLayout = (RelativeLayout) view.findViewById(R.id.infoLayout);
        int actionsWidth = actionsLayout.getWidth() - 2;

        actionsLayout.setVisibility(LinearLayout.VISIBLE);
        TranslateAnimation animation = new TranslateAnimation(0, actionsWidth, 0, 0);

        animation.setFillAfter(true);
        animation.setDuration(150);
        infoLayout.startAnimation(animation);
    }

    protected void hideAllActions() {
        int firstVisible = mListView.getFirstVisiblePosition();
        int lastVisible = mListView.getLastVisiblePosition();

        for (int i = 0; i <= (lastVisible - firstVisible); i++) {
            View view = mListView.getChildAt(i);

            if (view.findViewById(R.id.infoLayout).getAnimation() != null) {
                hideActions(view, true);
            }
        }
    }

    protected void hideActions(View view, Boolean animate) {
        final LinearLayout actionsLayout = (LinearLayout) view.findViewById(R.id.actionsLayout);
        final RelativeLayout infoLayout = (RelativeLayout) view.findViewById(R.id.infoLayout);

        if (animate) {
            int actionsWidth = AppUtil.dpToPx(MainActivity.this, 191);

            TranslateAnimation animation = new TranslateAnimation(actionsWidth, 0, 0, 0);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    infoLayout.clearAnimation();
                    actionsLayout.setVisibility(LinearLayout.INVISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });

            animation.setDuration(150);
            infoLayout.startAnimation(animation);
        } else {
            infoLayout.clearAnimation();
            actionsLayout.setVisibility(LinearLayout.INVISIBLE);
        }
    }

    protected void deleteCard(Card card) {
        int delete = mDataSource.delete(card);

        if (delete > 0) {
            updateList();
            AppUtil.showToast(this, getString(R.string.card_success_delete));
        }
    }

    protected void removeSwipe() {
        if (mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    protected void trackBalance(String balance, String number) {
        try {
            JSONObject props = new JSONObject();
            props.put("balance", Float.parseFloat(balance));
            props.put("tuc", number);

            mixpanelTrackEvent("Consulta Saldo", number, props);
        } catch (JSONException e) {}
    }

    private class GetMpesoBalances extends AsyncTask<Object, Void, List<CardBalanceContainer>> {

        private List<Card> mCards;
        private int mCount;

        private GetMpesoBalances(List<Card> cards) {
            mCards = cards;
        }

        @Override
        protected List<CardBalanceContainer> doInBackground(Object... objects) {
            try {
                MpesoService service = new MpesoService();
                List<CardBalanceContainer> list = new ArrayList<CardBalanceContainer>();

                for (Card card : mCards) {
                    MpesoBalance balance = service.loadBalanceSync(card);
                    CardBalanceContainer cardBalanceContainer = new CardBalanceContainer(card, balance);

                    list.add(cardBalanceContainer);
                }

                return list;
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<CardBalanceContainer> hashMaps) {
            if (hashMaps != null) {
                final int hashMapsSize = hashMaps.size();

                for (CardBalanceContainer hashMap : hashMaps) {
                    mCount++;
                    Card card = hashMap.card;
                    MpesoBalance mpesoBalance = hashMap.mpesoBalance;
                    String balance = AppUtil.parseBalance(mpesoBalance.Mensaje);

                    if (!mpesoBalance.Error && balance != null) {
                        card.setBalance(balance);
                        mDataSource.update(card);

                        trackBalance(card.getBalance(), card.getNumber());

                        SaldoTucService service = new SaldoTucService();
                        service.storeBalance(card, new Callback<Card>() {
                            @Override
                            public void success(Card card, Response response) {
                                checkIfLast(hashMapsSize);
                            }

                            @Override
                            public void failure(RetrofitError error) {
                                checkIfLast(hashMapsSize);
                            }
                        });
                    } else {
                        checkIfLast(hashMapsSize);
                        AppUtil.showToast(MainActivity.this, String.format(getString(R.string.card_invalid), AppUtil.formatCard(card.getNumber())));
                    }
                }
            } else {
                removeSwipe();
                AppUtil.showToast(MainActivity.this, getString(R.string.network_error));
            }
        }

        protected void checkIfLast(int hashMapsSize) {
            if (mCount == hashMapsSize) {
                removeSwipe();
                updateList();
            }
        }

    }

    private class CardBalanceContainer {

        protected Card card;
        protected MpesoBalance mpesoBalance;

        private CardBalanceContainer(Card card, MpesoBalance mpesoBalance) {
            this.card = card;
            this.mpesoBalance = mpesoBalance;
        }
    }

}
