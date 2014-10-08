package com.socialimprover.saldotuc;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.*;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.socialimprover.saldotuc.app.R;
import org.json.JSONException;
import org.json.JSONObject;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ActionBarActivity {

    public static final String TAG = MainActivity.class.getSimpleName();

    protected CardDataSource mDataSource;
    protected MixpanelAPI mMixpanel;

    protected ListView mListView;
    protected SwipeRefreshLayout mSwipeRefreshLayout;
    protected List<Card> mCards;
    protected Card mCard;
    protected View mCardView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setTitle(R.string.title_activity_main);

        mDataSource = SaldoTucApplication.getDatabaseHelper();
        mMixpanel = SaldoTucApplication.getMixpanelInstance(this);

        mListView = (ListView) findViewById(android.R.id.list);
        mListView.setOnItemClickListener(mOnItemClickListener);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(mOnRefreshListener);
        mSwipeRefreshLayout.setColorScheme(R.color.swipeRefresh1, R.color.swipeRefresh2, R.color.swipeRefresh3, R.color.swipeRefresh4);
    }

    @Override
    public void onResume() {
        super.onResume();

        updateList();
    }

    @Override
    protected void onDestroy() {
        mMixpanel.flush();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_card_add) {
            Intent intent = new Intent(this, CardAddActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
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
            mCardView = mListView.getChildAt(position);
            Card card = mCards.get(position);
            mCard = card;

            switch (view.getId()) {
                case R.id.cardActionBalance: // request balance
                    hideActions(mCardView, true);

                    if ( ! AppUtil.isNetworkAvailable(MainActivity.this)) {
                        AppUtil.showToast(MainActivity.this, getString(R.string.no_connection_message));
                    } else {
                        setSupportProgressBarIndeterminateVisibility(true);

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
                        removeProgressBar();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        removeProgressBar();
                        Log.e(TAG, "Error: " + error.getMessage());
                    }
                });
            } else {
                AppUtil.showToast(MainActivity.this, getString(R.string.card_invalid));
                removeProgressBar();
            }
        }

        @Override
        public void failure(RetrofitError error) {
            removeProgressBar();
            if (error.isNetworkError()) {
                AppUtil.showToast(MainActivity.this, getString(R.string.network_error));
            }
            Log.e(TAG, "Error: " + error.getMessage());
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
                            removeProgressBar();
                            deleteCard(mCard);
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            removeProgressBar();
                            Log.e(TAG, "Error: " + error.getMessage());
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
        for (int i = 0; i < mListView.getCount(); i++) {
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
                for (int i = 0; i < hashMaps.size(); i++) {
                    CardBalanceContainer item = hashMaps.get(i);
                    Card card = item.card;
                    MpesoBalance mpesoBalance = item.mpesoBalance;
                    String balance = AppUtil.parseBalance(mpesoBalance.Mensaje);
                    final int hashMapsSize = hashMaps.size();

                    if (!mpesoBalance.Error && balance != null) {
                        card.setBalance(balance);
                        mDataSource.update(card);
                        ((TextView) mListView.getChildAt(i).findViewById(R.id.cardBalance)).setText("C$ " + balance);

                        trackBalance(card.getBalance(), card.getNumber());

                        SaldoTucService service = new SaldoTucService();
                        service.storeBalance(card, new Callback<Card>() {
                            @Override
                            public void success(Card card, Response response) {
                                mCount++;

                                if (mCount == hashMapsSize) {
                                    if (mSwipeRefreshLayout.isRefreshing()) {
                                        mSwipeRefreshLayout.setRefreshing(false);
                                    }
                                }
                            }

                            @Override
                            public void failure(RetrofitError error) {
                                Log.e(TAG, "Error: " + error.getMessage());
                            }
                        });
                    } else {
                        AppUtil.showToast(MainActivity.this, getString(R.string.card_invalid));
                        removeProgressBar();
                    }
                }
            } else {
                if (mSwipeRefreshLayout.isRefreshing()) {
                    mSwipeRefreshLayout.setRefreshing(false);
                }
                AppUtil.showToast(MainActivity.this, getString(R.string.network_error));
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
