package com.socialimprover.saldotuc.app;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    protected SaldoTucDataSource mDataSource;

    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDataSource = new SaldoTucDataSource(this);
        mListView = (ListView) findViewById(android.R.id.list);
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
            card.setName(cursor.getString(1));
            card.setCard(cursor.getString(2));

            if ( ! cursor.isNull(6)) {
                card.setBalance(cursor.getInt(6));
            }

            cards.add(card);

            cursor.moveToNext();
        }

        CardAdapter adapter = new CardAdapter(this, cards);

        mListView.setAdapter(adapter);
    }

}
