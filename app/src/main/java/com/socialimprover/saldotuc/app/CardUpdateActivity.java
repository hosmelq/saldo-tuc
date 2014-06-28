package com.socialimprover.saldotuc.app;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class CardUpdateActivity extends ActionBarActivity {

    public static final String TAG = CardAddActivity.class.getSimpleName();

    protected CardDataSource mDataSource;
    protected Card mCard;
    protected String mPhoneOld = null;

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
        mCard = (Card) getIntent().getSerializableExtra("card");

        mNotificationLayout = (RelativeLayout) findViewById(R.id.notificationLayout);
        mName = (EditText) findViewById(R.id.nameField);
        mNumber = (EditText) findViewById(R.id.cardField);
        mNotificationCheckBox = (CheckBox) findViewById(R.id.notificationCheckBox);
        mPhone = (EditText) findViewById(R.id.phoneField);
        mHourSpinner = (Spinner) findViewById(R.id.hour_spinner);
        mAmPmSpinner = (Spinner) findViewById(R.id.ampm_spinner);

        mNotificationCheckBox.setOnCheckedChangeListener(mOnCheckedChangeListener);

        setHourAdapters();
        fillFields(mCard);
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
        getMenuInflater().inflate(R.menu.card_update, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_update) {
            String name = mName.getText().toString().trim();
            String card = mNumber.getText().toString().trim();
            String phone = mPhone.getText().toString().trim();
            String hour = mHourSpinner.getSelectedItem().toString();
            String ampm = mAmPmSpinner.getSelectedItem().toString();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(card) || card.length() != 8) {
                validationErrorMessage(getString(R.string.error_title), getString(R.string.card_add_error_message));
            } else {
                if ( ! mCard.getNumber().equals(card) && mDataSource.findByNumber(card).getCount() > 0) {
                    validationErrorMessage(getString(R.string.error_title), getString(R.string.card_add_duplicate_number_error_message));
                    return false;
                }

                mCard.setName(name);
                mCard.setNumber(card);

                if (mNotificationCheckBox.isChecked()) {
                    if (TextUtils.isEmpty(phone) || phone.length() != 8) {
                        validationErrorMessage(getString(R.string.error_title), getString(R.string.card_add_phone_error_message));
                    } else if (mCard.getPhone() != null && ! mCard.getPhone().equals(phone) && mDataSource.findByPhone(phone).getCount() > 0) {
                        validationErrorMessage(getString(R.string.error_title), getString(R.string.card_add_duplicate_phone_error_message));
                        return false;
                    } else {
                        setSupportProgressBarIndeterminateVisibility(true);

                        if (mCard.getPhone() != null) {
                            mPhoneOld = mCard.getPhone();
                        }

                        mCard.setPhone(phone);
                        mCard.setHour(hour);
                        mCard.setAmpm(ampm);

                        SaldoTucService service = new SaldoTucService();
                        service.updateCard(mCard, new Callback<Card>() {
                            @Override
                            public void success(Card card, Response response) {
                                removeProgressBar();

                                if (mCard.getPhone().equals(mPhoneOld)) {
                                    updateCard(mCard);
                                    finish();
                                } else {
                                    Intent intent = new Intent(CardUpdateActivity.this, PhoneVerificationActivity.class);
                                    intent.putExtra("action", "update");
                                    intent.putExtra("card", mCard);
                                    intent.putExtra("phone_old", mPhoneOld);
                                    startActivity(intent);
                                }
                            }

                            @Override
                            public void failure(RetrofitError error) {
                                removeProgressBar();
                                Log.e(TAG, "Error: " + error.getMessage());
                            }
                        });
                    }
                } else {
                    updateCard(mCard);
                    finish();
                }
            }
        }

        return super.onOptionsItemSelected(item);
    }

    protected void setHourAdapters() {
        ArrayAdapter<CharSequence> hourAdapter = ArrayAdapter.createFromResource(this, R.array.hours, android.R.layout.simple_spinner_item);
        hourAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mHourSpinner.setAdapter(hourAdapter);

        ArrayAdapter<CharSequence> amPmAdapter = ArrayAdapter.createFromResource(this, R.array.ampm, android.R.layout.simple_spinner_item);
        amPmAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mAmPmSpinner.setAdapter(amPmAdapter);
    }

    protected void fillFields(Card card) {
        mName.setText(card.getName());
        mNumber.setText(card.getNumber());

        if (card.getPhone() != null) {
            mNotificationCheckBox.setChecked(true);
//            mNotificationLayout.setVisibility(RelativeLayout.VISIBLE);
            mPhone.setText(card.getPhone());
            mHourSpinner.setSelection(Integer.parseInt(card.getHour()) - 1);

            if (card.getAmpm().equals("a.m.")) {
                mAmPmSpinner.setSelection(0);
            } else {
                mAmPmSpinner.setSelection(1);
            }
        }
    }

    protected void validationErrorMessage(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    protected void updateCard(Card card) {
        mDataSource.update(card);

        Toast.makeText(this, R.string.card_success_update, Toast.LENGTH_LONG).show();
    }

    protected void removeProgressBar() {
        setSupportProgressBarIndeterminateVisibility(false);
    }

    protected CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        if (isChecked) {
            mNotificationLayout.setVisibility(RelativeLayout.VISIBLE);
        } else {
            mNotificationLayout.setVisibility(RelativeLayout.INVISIBLE);
        }
        }
    };

}
