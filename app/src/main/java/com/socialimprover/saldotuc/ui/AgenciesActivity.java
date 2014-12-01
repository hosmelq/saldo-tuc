package com.socialimprover.saldotuc.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.socialimprover.saldotuc.app.R;
import com.socialimprover.saldotuc.models.Agency;
import com.socialimprover.saldotuc.sync.SaldoTucService;
import com.socialimprover.saldotuc.util.AppUtil;

import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class AgenciesActivity extends BaseActivity {

    public static final String TAG = AgenciesActivity.class.getSimpleName();

    protected ListView mListView;
    protected List<Agency> mAgencies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setActionBarTitle(getString(R.string.title_activity_agencies));

        mListView = (ListView) findViewById(android.R.id.list);
        mListView.setOnItemClickListener(mOnItemClickListener);

        showProgressBar();

        SaldoTucService service = new SaldoTucService();
        service.getAgencies(mAgenciesCallback);
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_agencies;
    }

    protected Callback<List<Agency>> mAgenciesCallback = new Callback<List<Agency>>() {
        @Override
        public void success(List<Agency> agencies, Response response) {
            hideProgressBar();
            updateList(agencies);
            mAgencies = agencies;
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
            intent.putExtra("agency", agency);
            startActivity(intent);
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

}
