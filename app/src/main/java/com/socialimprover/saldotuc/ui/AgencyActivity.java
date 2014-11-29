package com.socialimprover.saldotuc.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import com.socialimprover.saldotuc.app.R;
import com.socialimprover.saldotuc.models.Agency;
import com.socialimprover.saldotuc.models.District;

public class AgencyActivity extends BaseActivity {

    public static final String TAG = AgencyActivity.class.getSimpleName();

    District mDistrict;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setActionBarTitle(getString(R.string.title_activity_agency));

        Agency agency = (Agency) getIntent().getSerializableExtra("agency");
        mDistrict = (District) getIntent().getSerializableExtra("district");

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
        int id = item.getItemId();

        if (id == android.R.id.home) {
            Intent intent = new Intent().putExtra("district", mDistrict);
            setResult(RESULT_OK, intent);
            finish();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
