package com.socialimprover.saldotuc.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.socialimprover.saldotuc.app.R;
import com.socialimprover.saldotuc.models.Card;
import com.socialimprover.saldotuc.provider.CardDataSource;
import com.socialimprover.saldotuc.util.AppUtil;

public class CardAddActivity extends BaseActivity {

    public static final String TAG = CardAddActivity.class.getSimpleName();

    protected CardDataSource mCardDataSource;
    protected EditText mName;
    protected EditText mNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getToolbar().setNavigationIcon(R.drawable.ic_close_white);

        mCardDataSource = new CardDataSource(this);
        mName = (EditText) findViewById(R.id.nameField);
        mNumber = (EditText) findViewById(R.id.cardField);

        mNumber.setOnEditorActionListener(mEnterListener);
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_card_add;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.card_add, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_save) {
            processCard();
        }

        return super.onOptionsItemSelected(item);
    }

    protected TextView.OnEditorActionListener mEnterListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
            if ((keyEvent != null && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER) || i == EditorInfo.IME_ACTION_DONE) {
                processCard();
            }

            return false;
        }
    };

    protected boolean processCard() {
        String name = mName.getText().toString().trim();
        String cardNumber = mNumber.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(cardNumber) || cardNumber.length() != 8) {
            AppUtil.showDialog(CardAddActivity.this, getString(R.string.error_title), getString(R.string.card_add_error_message));
        } else {
            if (mCardDataSource.findByNumber(cardNumber) != null) {
                AppUtil.showDialog(CardAddActivity.this, getString(R.string.error_title), getString(R.string.card_add_duplicate_number_error_message));
                return false;
            }

            Card card = new Card();
            card.setName(name);
            card.setNumber(cardNumber);

            mCardDataSource.create(card);

            Intent intent = new Intent();
            intent.putExtra("message", getString(R.string.card_success_save));
            setResult(RESULT_OK, intent);
            finish();
        }

        return true;
    }

}
