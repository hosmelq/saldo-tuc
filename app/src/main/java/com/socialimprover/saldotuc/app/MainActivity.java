package com.socialimprover.saldotuc.app;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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


public class MainActivity extends ListActivity {

    public static final String TAG = MainActivity.class.getSimpleName();

    protected SaldoTucDataSource mDataSource;
    protected List<Card> mCards;
    protected Card mCard;
    protected View mCardView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        BalanceService service = new BalanceService();
        Card card = mCards.get(position);
        mCard = card;
        mCardView = v;
        service.loadBalance(card, mBalanceCallback);
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
                card.setBalance(cursor.getInt(6));
            }

            cards.add(card);

            cursor.moveToNext();
        }

        mCards = cards;
        CardAdapter adapter = new CardAdapter(this, cards);

        setListAdapter(adapter);
    }

    protected Callback<Balance> mBalanceCallback = new Callback<Balance>() {
        @Override
        public void success(Balance balance, Response response) {
            Pattern pattern = Pattern.compile("[0-9]+(?:\\.[0-9]*)?");
            Matcher matcher = pattern.matcher(balance.Mensaje);

            if (matcher.find()) {
                String match = matcher.group(0);
                mDataSource.updateCard(mCard.getId(), match);
                ((TextView) mCardView.findViewById(R.id.cardBalance)).setText("C$ " + match);
            }
        }

        @Override
        public void failure(RetrofitError error) {
            Log.e(TAG, "Error: " + error.getMessage());
        }
    };

}
