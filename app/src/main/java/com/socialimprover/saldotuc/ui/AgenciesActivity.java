package com.socialimprover.saldotuc.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.socialimprover.saldotuc.app.R;
import com.socialimprover.saldotuc.models.Agency;
import com.socialimprover.saldotuc.models.District;
import com.socialimprover.saldotuc.sync.SaldoTucService;
import com.socialimprover.saldotuc.util.AppUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class AgenciesActivity extends BaseActivity {

    public static final String TAG = AgenciesActivity.class.getSimpleName();

    protected ListView mListView;
    protected List<Agency> mAgencies;
    protected District mDistrict;
    protected final int REQUEST_CODE_DISTRICT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDistrict = (District) getIntent().getSerializableExtra("district");

        mListView = (ListView) findViewById(android.R.id.list);
        mListView.setOnItemClickListener(mOnItemClickListener);

        setActionBarTitle(String.format(getString(R.string.title_activity_agencies), mDistrict.getName()));

        showProgressBar();

        SaldoTucService service = new SaldoTucService();
        service.getAgenciesByDistrict(mDistrict, mAgenciesCallback);
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_agencies;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_DISTRICT) {
            mDistrict = (District) data.getSerializableExtra("district");
        }
    }

    protected Callback<List<Agency>> mAgenciesCallback = new Callback<List<Agency>>() {
        @Override
        public void success(List<Agency> agencies, Response response) {
            hideProgressBar();
            updateList(agencies);
            mAgencies = agencies;
            trackViewDistrict();
        }

        @Override
        public void failure(RetrofitError error) {
            hideProgressBar();
            if (error.isNetworkError()) {
                AppUtil.showToast(AgenciesActivity.this, getString(R.string.network_error));
            }
            finish();
        }
    };

    protected AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            Agency agency = mAgencies.get(position);

            Intent intent = new Intent(AgenciesActivity.this, AgencyActivity.class);
            intent.putExtra("district", mDistrict);
            intent.putExtra("agency", agency);
            startActivityForResult(intent, REQUEST_CODE_DISTRICT);
        }
    };

    protected void updateList(List<Agency> agencies) {
        if (mListView.getAdapter() == null) {
            AgencyAdapter adapter = new AgencyAdapter(this, agencies);
            mListView.setAdapter(adapter);
        } else {
            ( (AgencyAdapter) mListView.getAdapter()).refill(agencies);
        }
    }

    private void trackViewDistrict() {
        try {
            JSONObject props = new JSONObject();
            props.put("district", mDistrict.getName());

            mixpanelTrackEvent("View District", null, props);
        } catch (JSONException e) {}
    }

}
