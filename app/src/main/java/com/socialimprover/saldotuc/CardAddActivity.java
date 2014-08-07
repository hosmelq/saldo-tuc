package com.socialimprover.saldotuc;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import com.socialimprover.saldotuc.app.R;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class CardAddActivity extends ActionBarActivity {

    public static final String TAG = CardAddActivity.class.getSimpleName();

    protected CardDataSource mDataSource;
    protected Card mNewCard;

    protected RelativeLayout mNotificationLayout;
    protected EditText mName;
    protected EditText mNumber;
    protected CheckBox mNotificationCheckBox;
    protected EditText mPhone;
    protected Spinner mHourSpinner;
    protected Spinner mAmPmSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_card_add);

        mDataSource = new CardDataSource(this);

        mNotificationLayout = (RelativeLayout) findViewById(R.id.notificationLayout);
        mName = (EditText) findViewById(R.id.nameField);
        mNumber = (EditText) findViewById(R.id.cardField);
        mNotificationCheckBox = (CheckBox) findViewById(R.id.notificationCheckBox);
        mPhone = (EditText) findViewById(R.id.phoneField);
        mHourSpinner = (Spinner) findViewById(R.id.hour_spinner);
        mAmPmSpinner = (Spinner) findViewById(R.id.ampm_spinner);

        mNumber.setOnEditorActionListener(mEnterListener);
        mNotificationCheckBox.setOnCheckedChangeListener(mOnCheckedChangeListener);

        setHourAdapters();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDataSource.open();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDataSource.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.card_add, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
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

    protected CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
            if (AppUtil.isNetworkAvailable(CardAddActivity.this)) {
                if (isChecked) {
                    mNotificationLayout.setVisibility(RelativeLayout.VISIBLE);
                } else {
                    mNotificationLayout.setVisibility(RelativeLayout.GONE);
                }
            } else {
                AppUtil.showDialog(CardAddActivity.this, getString(R.string.error_title), getString(R.string.card_add_sms_no_internet_error_message));
                mNotificationCheckBox.setChecked(false);
            }
        }
    };

    protected void setHourAdapters() {
        ArrayAdapter<CharSequence> hourAdapter = ArrayAdapter.createFromResource(this, R.array.hours, android.R.layout.simple_spinner_item);
        hourAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mHourSpinner.setAdapter(hourAdapter);

        ArrayAdapter<CharSequence> amPmAdapter = ArrayAdapter.createFromResource(this, R.array.ampm, android.R.layout.simple_spinner_item);
        amPmAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mAmPmSpinner.setAdapter(amPmAdapter);
    }

    protected boolean processCard() {
        String name = mName.getText().toString().trim();
        String card = mNumber.getText().toString().trim();
        String phone = mPhone.getText().toString().trim();
        String hour = mHourSpinner.getSelectedItem().toString();
        String ampm = mAmPmSpinner.getSelectedItem().toString();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(card) || card.length() != 8) {
            AppUtil.showDialog(CardAddActivity.this, getString(R.string.error_title), getString(R.string.card_add_error_message));
        } else {
            if (mDataSource.findByNumber(card).getCount() > 0) {
                AppUtil.showDialog(CardAddActivity.this, getString(R.string.error_title), getString(R.string.card_add_duplicate_number_error_message));
                return true;
            }

            mNewCard = new Card();
            mNewCard.setName(name);
            mNewCard.setNumber(card);

            if (mNotificationCheckBox.isChecked()) {
                if (TextUtils.isEmpty(phone) || phone.length() != 8) {
                    AppUtil.showDialog(CardAddActivity.this, getString(R.string.error_title), getString(R.string.card_add_phone_error_message));
                } else if (mDataSource.findByPhone(phone).getCount() > 0) {
                    AppUtil.showDialog(CardAddActivity.this, getString(R.string.error_title), getString(R.string.card_add_duplicate_phone_error_message));
                    return true;
                } else {
                    setSupportProgressBarIndeterminateVisibility(true);

                    mNewCard.setPhone(phone);
                    mNewCard.setHour(hour);
                    mNewCard.setAmpm(ampm);

                    SaldoTucService service = new SaldoTucService();
                    service.storeCard(mNewCard, new Callback<Card>() {
                        @Override
                        public void success(Card card, Response response) {
                            removeProgressBar();

                            Intent intent = new Intent(CardAddActivity.this, PhoneVerificationActivity.class);
                            intent.putExtra("action", "create");
                            intent.putExtra("card", mNewCard);
                            startActivity(intent);
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            removeProgressBar();
                            Log.e(TAG, "Error: " + error.getMessage());
                        }
                    });
                }
            } else {
                createCard(mNewCard);
                finish();
            }
        }
        return false;
    }

    protected void createCard(Card card) {
        mDataSource.create(card);

        AppUtil.showToast(this, getString(R.string.card_success_save));
    }

    protected void removeProgressBar() {
        setSupportProgressBarIndeterminateVisibility(false);
    }

}
