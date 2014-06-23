package com.socialimprover.saldotuc.app;

import android.app.AlertDialog;
import android.content.DialogInterface;
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

    protected CardDataSource mDataSource;
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
        mListView.setOnItemLongClickListener(mOnItemLongClickListener);

        mDataSource = new CardDataSource(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mDataSource.open();

        Cursor cursor = mDataSource.all();
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

        mCards = cards;

        if (mListView.getAdapter() == null) {
            CardAdapter adapter = new CardAdapter(this, cards);
            mListView.setAdapter(adapter);
        } else {
            ( (CardAdapter) mListView.getAdapter()).refill(cards);
        }

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
                mCard.setBalance(balance);
                mDataSource.update(mCard);
                ((TextView) mCardView.findViewById(R.id.cardBalance)).setText("C$ " + balance);

                Cursor cursor = mDataSource.find(mCard.getId());
                cursor.moveToFirst();
                SaldoTucService service = new SaldoTucService();

                Card card = new Card();
                card.setNumber(cursor.getString(2));
                card.setBalance(cursor.getString(6));

                service.storeBalance(card, new Callback<Card>() {
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

    protected AdapterView.OnItemLongClickListener mOnItemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setItems(R.array.card_choices, mDialogListener);
            AlertDialog dialog = builder.create();
            dialog.show();

            mCard = mCards.get(position);

            return true;
        }
    };

    protected DialogInterface.OnClickListener mDialogListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int which) {
            switch (which) {
                case 0: // update card
                    Intent intent = new Intent(MainActivity.this, CardUpdateActivity.class);
                    intent.putExtra("card", mCard);
                    startActivity(intent);
                    break;
                case 1: // delete card
                    String number = mCard.getNumber().substring(0, 4) + "-" + mCard.getNumber().substring(4, 8);
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

    protected DialogInterface.OnClickListener mDeleteCardDialogListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            int delete = mDataSource.delete(mCard);

            if (delete > 0) {
                mCards.remove(mCard);
                ( (CardAdapter) mListView.getAdapter()).refill(mCards);
//                ( (CardAdapter) mListView.getAdapter()).notifyDataSetChanged();
            }
        }
    };

    private void removeProgressBar() {
        setSupportProgressBarIndeterminateVisibility(false);
    }

}
