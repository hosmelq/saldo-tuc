package com.socialimprover.saldotuc.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class PhoneVerificationActivity extends ActionBarActivity {

    public static final String TAG = PhoneVerificationActivity.class.getSimpleName();

    protected CardDataSource mDataSource;

    protected Card mCard;
    protected EditText mCode;
    protected String mPhoneOld;
    protected String mAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_phone_verification);

        mDataSource = new CardDataSource(this);

        mCard = (Card) getIntent().getSerializableExtra("card");
        mCode = (EditText) findViewById(R.id.codeField);
        mPhoneOld = getIntent().getExtras().getString("phone_old");
        mAction = getIntent().getExtras().getString("action");

        String phone = mCard.getPhone().substring(0, 4) + "-" + mCard.getPhone().substring(4, 8);
        ( (TextView) findViewById(R.id.textInfo)).append(" " + phone);
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
        getMenuInflater().inflate(R.menu.phone_verification, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_verify) {
            String code = mCode.getText().toString().trim();

            if (TextUtils.isEmpty(code)) {
                AppUtil.createDialog(PhoneVerificationActivity.this, getString(R.string.error_title), getString(R.string.code_verification_error_message));
            } else {
                setSupportProgressBarIndeterminateVisibility(true);

                SaldoTucService service = new SaldoTucService();
                service.verifyCard(mCard.getPhone(), mPhoneOld, code, new Callback<Card>() {
                    @Override
                    public void success(Card card, Response response) {
                        removeProgressBar();

                        if (mAction.equals("create")) {
                            mDataSource.create(mCard);
                            Toast.makeText(PhoneVerificationActivity.this, R.string.card_success_save, Toast.LENGTH_LONG).show();
                        } else {
                            mDataSource.update(mCard);
                            Toast.makeText(PhoneVerificationActivity.this, R.string.card_success_update, Toast.LENGTH_LONG).show();
                        }

                        Intent intent = new Intent(PhoneVerificationActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        removeProgressBar();

                        if (error.getResponse().getStatus() == 401) {
                            AppUtil.createDialog(PhoneVerificationActivity.this, getString(R.string.error_title), getString(R.string.code_verification_error_message));
                        }

                        Log.e(TAG, "Error: " + error.getMessage());
                    }
                });
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // super.onBackPressed();
    }

    protected void removeProgressBar() {
        setSupportProgressBarIndeterminateVisibility(false);
    }
}
