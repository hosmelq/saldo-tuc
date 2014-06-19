package com.socialimprover.saldotuc.app;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class MainActivity extends ActionBarActivity {

    public static final String TAG = MainActivity.class.getSimpleName();

    protected SaldoTucDataSource mDataSource;
    protected ListView mListView;
    protected List<Card> mCards;
    protected Card mCard;
    protected View mCardView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_main);

        mListView = (ListView) findViewById(R.id.cardList);
        mListView.setOnItemClickListener(mOnItemClickListener);

        mDataSource = new SaldoTucDataSource(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mDataSource.open();

        Cursor cursor = mDataSource.selectAllCards();
        updateList(cursor);
    }

    @Override
    public void onPause() {
        super.onPause();
        mDataSource.close();
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

    protected void updateList(Cursor cursor) {
        List<Card> cards = new ArrayList<Card>();

        cursor.moveToFirst();

        while( ! cursor.isAfterLast() ) {
            Card card = new Card();

            card.setId(cursor.getInt(0));
            card.setName(cursor.getString(1));
            card.setCard(cursor.getString(2));

            if ( ! cursor.isNull(6)) {
                card.setBalance(cursor.getString(6));
            }

            cards.add(card);

            cursor.moveToNext();
        }

        mCards = cards;
        CardAdapter adapter = new CardAdapter(this, cards);

        mListView.setAdapter(adapter);
    }

    protected AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            setSupportProgressBarIndeterminateVisibility(true);

            MpesoService service = new MpesoService();
            Card card = mCards.get(position);
            mCard = card;
            mCardView = view;
            service.loadBalance(card, mBalanceCallback);
        }
    };

    protected Callback<MpesoBalance> mBalanceCallback = new Callback<MpesoBalance>() {
        @Override
        public void success(MpesoBalance mpesoBalance, Response response) {
            Pattern pattern = Pattern.compile("[0-9]+(?:\\.[0-9]*)?");
            Matcher matcher = pattern.matcher(Html.fromHtml(mpesoBalance.Mensaje).toString());

            if ( ! mpesoBalance.Error && matcher.find()) {
                String balance = matcher.group(0);
                mDataSource.updateCardBalance(mCard.getId(), balance);
                ((TextView) mCardView.findViewById(R.id.cardBalance)).setText("C$ " + balance);

                Cursor cursor = mDataSource.selectCard(mCard.getId());
                cursor.moveToFirst();
                SaldoTucService service = new SaldoTucService();

                Card card = new Card();
                card.setCard(cursor.getString(2));
                card.setPhone(cursor.getString(3));
                card.setHour(cursor.getString(4));
                card.setAmpm(cursor.getString(5));

                service.storeCard(card, new Callback<Card>() {
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
                Toast.makeText(MainActivity.this, R.string.card_invalidad, Toast.LENGTH_LONG).show();
                removeProgressBar();
            }
        }

        @Override
        public void failure(RetrofitError error) {
            removeProgressBar();
            Log.e(TAG, "Error: " + error.getMessage());
        }
    };

    private void removeProgressBar() {
        setSupportProgressBarIndeterminateVisibility(false);
    }

}
