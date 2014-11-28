package com.socialimprover.saldotuc;

import android.os.Bundle;
import android.widget.ListView;

import com.socialimprover.saldotuc.app.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class AgencyActivity extends BaseActivity {

    public static final String TAG = AgencyActivity.class.getSimpleName();
    protected Districts.District mDistrict;
    protected ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDistrict = (Districts.District) getIntent().getSerializableExtra("district");
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

    protected Callback<Agencies> mAgenciesCallback = new Callback<Agencies>() {
        @Override
        public void success(Agencies agencies, Response response) {
            hideProgressBar();
            updateList(agencies.data);
            trackViewDistrict();
        }

        @Override
        public void failure(RetrofitError error) {
            hideProgressBar();
            if (error.isNetworkError()) {
                AppUtil.showToast(AgencyActivity.this, getString(R.string.network_error));
            }
            finish();
        }
    };

    protected void updateList(List<Agencies.Agency> agencies) {
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
