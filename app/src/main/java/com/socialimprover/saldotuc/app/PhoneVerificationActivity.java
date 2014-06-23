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
import android.widget.EditText;
import android.widget.TextView;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class PhoneVerificationActivity extends ActionBarActivity {

    public static final String TAG = PhoneVerificationActivity.class.getSimpleName();

    protected String mPhone;
    protected EditText mCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_phone_verification);

        mPhone = getIntent().getExtras().getString("phone");
        mCode = (EditText) findViewById(R.id.codeField);

        String phone = mPhone.substring(0, 4) + "-" + mPhone.substring(4, 8);
        ( (TextView) findViewById(R.id.textInfo)).append(" " + phone);
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
                validationErrorMessage(getString(R.string.error_title), getString(R.string.code_verification_error_message));
            } else {
                setSupportProgressBarIndeterminateVisibility(true);

                SaldoTucService service = new SaldoTucService();
                service.verifyCard(mPhone, code, new Callback<Card>() {
                    @Override
                    public void success(Card card, Response response) {
                        removeProgressBar();

                        if (response.getStatus() == 200) {
                            Intent intent = new Intent(PhoneVerificationActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        removeProgressBar();

                        if (error.getResponse().getStatus() == 401) {
                            validationErrorMessage(getString(R.string.error_title), getString(R.string.code_verification_error_message));
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

    protected void validationErrorMessage(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    protected void removeProgressBar() {
        setSupportProgressBarIndeterminateVisibility(false);
    }
}
