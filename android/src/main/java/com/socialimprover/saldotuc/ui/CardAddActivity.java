package com.socialimprover.saldotuc.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.parse.ParsePush;
import com.socialimprover.saldotuc.R;
import com.socialimprover.saldotuc.api.SaldoTucContract.CardColumns;
import com.socialimprover.saldotuc.model.Card;
import com.socialimprover.saldotuc.util.SyncHelper;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnEditorAction;
import butterknife.OnTextChanged;

import static com.socialimprover.saldotuc.util.LogUtils.makeLogTag;

public class CardAddActivity extends BaseActivity {
    public static final String TAG = makeLogTag("CardAddActivity");
    private final int NUMBER_COUNTER_MAX_LENGTH = 8;

    @Bind(R.id.cardNameInputLayout) TextInputLayout mCardNameInputLayout;
    @Bind(R.id.cardNameInput) EditText mCardNameInput;
    @Bind(R.id.cardNumberInputLayout) TextInputLayout mCardNumberInputLayout;
    @Bind(R.id.cardNumberInput) EditText mCardNumberInput;
    @Bind(R.id.cardSubscribe) SwitchCompat mCardSubscribe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_add);
        ButterKnife.bind(this);

        final Toolbar toolbar = getActionBarToolbar();
        toolbar.setNavigationIcon(R.drawable.ic_close_white_24dp);
        toolbar.setNavigationOnClickListener(view -> finish());

        mCardNumberInputLayout.setCounterMaxLength(NUMBER_COUNTER_MAX_LENGTH);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.card_add, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_save) {
            storeCard();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @OnTextChanged(R.id.cardNameInput)
    protected void nameTextChanged(CharSequence text) {
        validateName(text.toString());
    }

    @OnTextChanged(R.id.cardNumberInput)
    protected void numberTextChanged(CharSequence text) {
        validateNumber(text.toString());
    }

    @OnEditorAction(R.id.cardNumberInput)
    protected boolean numberEditorActionListener(TextView textView, int i, KeyEvent keyEvent) {
        if ((keyEvent != null && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER) || i == EditorInfo.IME_ACTION_DONE) {
            if (!validateNumber(textView.getText().toString())) {
                return true;
            }

            storeCard();
        }

        return false;
    }

    private void storeCard() {
        final String name = mCardNameInput.getText().toString().trim();
        final String number = mCardNumberInput.getText().toString().trim();

        if (!validateName(name) || !validateNumber(number)) {
            return;
        }

        Card.getQuery()
            .whereEqualTo(CardColumns.COLUMN_NUMBER, number)
            .fromLocalDatastore()
            .getFirstInBackground((object, e) -> {
                if (e == null) {
                    new AlertDialog.Builder(CardAddActivity.this)
                        .setTitle(R.string.card_add_error_title)
                        .setMessage(getString(R.string.card_already_exist_error))
                        .setPositiveButton(android.R.string.ok, null)
                        .create()
                        .show();

                    return;
                }

                Intent intent = new Intent();
                Card card = new Card();
                card.setBalance("");
                card.setName(name);
                card.setNumber(number);
                card.setUuid();

                if (mCardSubscribe.isChecked()) {
                    if (!SyncHelper.isOnline(CardAddActivity.this)) {
                        intent.putExtra(CardsActivity.WILL_SUBSCRIBE_WHEN_ONLINE, true);
                    }

                    card.setNotifications(true);
                    ParsePush.subscribeInBackground(card.getChannelName());
                } else {
                    card.setNotifications(false);
                }

                card.pinInBackground(e1 -> {
                    if (e1 == null) {
                        setResult(RESULT_OK, intent);
                    } else {
                        setResult(RESULT_CANCELED, intent);
                    }

                    finish();
                });
            });
    }

    private boolean validateName(String name) {
        if (name.trim().isEmpty()) {
            if (mCardNameInputLayout.getError() == null) {
                mCardNameInputLayout.setError(getString(R.string.card_name_error));
            }

            return false;
        }

        mCardNameInputLayout.setError(null);
        mCardNameInputLayout.setErrorEnabled(false);

        return true;
    }

    private boolean validateNumber(String number) {
        number = number.trim();

        if (number.isEmpty()) {
            String numberError = getString(R.string.card_number_error);

            if (mCardNumberInputLayout.getError() != numberError) {
                mCardNumberInputLayout.setError(numberError);
            }

            return false;
        }

        if (number.length() < NUMBER_COUNTER_MAX_LENGTH) {
            String numberLengthError = getString(R.string.card_number_length_error);

            if (mCardNumberInputLayout.getError() != numberLengthError) {
                mCardNumberInputLayout.setError(numberLengthError);
            }

            return false;
        }

        mCardNumberInputLayout.setError(null);
        mCardNumberInputLayout.setErrorEnabled(false);

        return true;
    }
}
