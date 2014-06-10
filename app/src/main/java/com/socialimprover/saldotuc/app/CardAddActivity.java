package com.socialimprover.saldotuc.app;

import android.app.AlertDialog;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;


public class CardAddActivity extends ActionBarActivity {

    protected EditText mName;
    protected EditText mCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_add);

        mName = (EditText)findViewById(R.id.nameField);
        mCard = (EditText)findViewById(R.id.cardField);
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

        if (id == R.id.action_send) {
            String name = mName.getText().toString().trim();
            String card = mCard.getText().toString().trim();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(card)) {
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
}
