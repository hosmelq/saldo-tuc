package com.socialimprover.saldotuc;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.socialimprover.saldotuc.app.R;

import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class DistrictsActivity extends BaseActivity {

    public static final String TAG = DistrictsActivity.class.getSimpleName();

    protected ListView mListView;
    protected List<Districts.District> mDistricts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setActionBarTitle(R.string.title_activity_districts);

        mListView = (ListView) findViewById(android.R.id.list);
        mListView.setOnItemClickListener(mOnItemClickListener);

        showProgressBar();

        SaldoTucService service = new SaldoTucService();
        service.getDistricts(mDistrictsCallback);
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_districts;
    }

    protected Callback<Districts> mDistrictsCallback = new Callback<Districts>() {
        @Override
        public void success(Districts districts, Response response) {
            hideProgressBar();
            mDistricts = districts.data;
            updateList(mDistricts);
        }

        @Override
        public void failure(RetrofitError error) {
            hideProgressBar();
            if (error.isNetworkError()) {
                AppUtil.showToast(DistrictsActivity.this, getString(R.string.network_error));
            }
            finish();
        }
    };

    protected AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            Districts.District district = mDistricts.get(position);

            Intent intent = new Intent(DistrictsActivity.this, AgencyActivity.class);
            intent.putExtra("district", district);
            startActivity(intent);
        }
    };

    protected void updateList(List<Districts.District> districts) {
        if (mListView.getAdapter() == null) {
            DistrictAdapter adapter = new DistrictAdapter(this, districts);
            mListView.setAdapter(adapter);
        } else {
            ( (DistrictAdapter) mListView.getAdapter()).refill(districts);
        }
    }

}
