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
import android.widget.RelativeLayout;
import android.widget.TextView;

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

        updateList();
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

    protected void updateList() {
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
            RelativeLayout relativeLayout = (RelativeLayout) view.findViewById(R.id.infoLayout);
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) relativeLayout.getLayoutParams();
            int actionsWidth = AppUtil.dpToPx(MainActivity.this, 199);
            int margin;

            if (layoutParams.leftMargin == actionsWidth) {
                margin = AppUtil.dpToPx(MainActivity.this, 8);
            } else {
                margin = actionsWidth;
            }

            layoutParams.leftMargin = margin;
            relativeLayout.setLayoutParams(layoutParams);

            if ( ! AppUtil.isNetworkAvailable(MainActivity.this)) {
                AppUtil.showToast(MainActivity.this, getString(R.string.no_connection_message));
            } else {
                setSupportProgressBarIndeterminateVisibility(true);

                MpesoService service = new MpesoService();
                Card card = mCards.get(position);
                mCard = card;
                mCardView = view;
                service.loadBalance(card, mBalanceCallback);
            }
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
                AppUtil.showToast(MainActivity.this, getString(R.string.card_invalidad));
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

    protected void deleteCard(Card card) {
        int delete = mDataSource.delete(card);

        if (delete > 0) {
//            mCards.remove(mCard);
//            ( (CardAdapter) mListView.getAdapter()).refill(mCards);
            updateList();
            AppUtil.showToast(this, getString(R.string.card_success_delete));
        }
    }

    private void removeProgressBar() {
        setSupportProgressBarIndeterminateVisibility(false);
    }

}
