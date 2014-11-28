package com.socialimprover.saldotuc.ui;

import android.os.Bundle;
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
    protected District mDistrict;
    protected ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDistrict = (District) getIntent().getSerializableExtra("district");
        mListView = (ListView) findViewById(android.R.id.list);

        setActionBarTitle(String.format(getString(R.string.title_activity_agency), mDistrict.getName()));

        showProgressBar();

        SaldoTucService service = new SaldoTucService();
        service.getAgenciesByDistrict(mDistrict, mAgenciesCallback);
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_agency;
    }

    protected Callback<List<Agency>> mAgenciesCallback = new Callback<List<Agency>>() {
        @Override
        public void success(List<Agency> agencies, Response response) {
            hideProgressBar();
            updateList(agencies);
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
