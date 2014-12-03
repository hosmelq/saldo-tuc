package com.socialimprover.saldotuc.ui;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import com.socialimprover.saldotuc.app.R;
import com.socialimprover.saldotuc.models.Agency;

public class AgencyActivity extends BaseActivity {

    public static final String TAG = AgencyActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setActionBarTitle(getString(R.string.title_activity_agency));

        Agency agency = (Agency) getIntent().getSerializableExtra("agency");

        ( (TextView) findViewById(R.id.nameText)).setText(agency.getName());
        ( (TextView) findViewById(R.id.neighborhoodText)).setText(agency.getNeighborhood());
        ( (TextView) findViewById(R.id.addressText)).setText(agency.getAddress());
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_agency;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
