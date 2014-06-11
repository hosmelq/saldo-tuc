package com.socialimprover.saldotuc.app;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;


public class CardAddActivity extends ActionBarActivity {

    protected EditText mName;
    protected EditText mCard;
    protected EditText mPhone;
    protected Spinner mHourSpinner;
    protected Spinner mAmPmSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_add);

        mName = (EditText) findViewById(R.id.nameField);
        mCard = (EditText) findViewById(R.id.cardField);
        mPhone = (EditText) findViewById(R.id.phoneField);
        mHourSpinner = (Spinner) findViewById(R.id.hour_spinner);
        mAmPmSpinner = (Spinner) findViewById(R.id.ampm_spinner);

        setHourAdapters();
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
            String name = mName.getText().toString().trim();
            String card = mCard.getText().toString().trim();
            String phone = mPhone.getText().toString().trim();
            String hour = mHourSpinner.getSelectedItem().toString();
            String ampm = mAmPmSpinner.getSelectedItem().toString();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(card) || card.length() != 8) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.card_add_error_title)
                    .setMessage(R.string.card_add_error_message)
                    .setPositiveButton(android.R.string.ok, null);

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    public void setHourAdapters() {
        ArrayAdapter<CharSequence> hourAdapter = ArrayAdapter.createFromResource(this, R.array.hours, android.R.layout.simple_spinner_item);
        hourAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mHourSpinner.setAdapter(hourAdapter);

        ArrayAdapter<CharSequence> amPmAdapter = ArrayAdapter.createFromResource(this, R.array.ampm, android.R.layout.simple_spinner_item);
        amPmAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mAmPmSpinner.setAdapter(amPmAdapter);
    }
}
