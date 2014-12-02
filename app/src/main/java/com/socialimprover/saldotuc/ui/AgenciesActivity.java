package com.socialimprover.saldotuc.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.socialimprover.saldotuc.app.R;
import com.socialimprover.saldotuc.models.Agency;
import com.socialimprover.saldotuc.provider.AgencyDataSource;
import com.socialimprover.saldotuc.sync.SaldoTucService;
import com.socialimprover.saldotuc.util.AppUtil;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class AgenciesActivity extends BaseActivity {

    public static final String TAG = AgenciesActivity.class.getSimpleName();

    protected AgencyDataSource mAgencyDataSource;
    protected ListView mListView;
    protected List<Agency> mAgencies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAgencyDataSource = new AgencyDataSource(this);

        setActionBarTitle(getString(R.string.title_activity_agencies));

        mListView = (ListView) findViewById(android.R.id.list);
        mListView.setOnItemClickListener(mOnItemClickListener);

        if (needSync()) {
            showProgressBar();

            SaldoTucService service = new SaldoTucService();
            service.getAgencies(mAgenciesCallback);
        } else {
            mAgencies = mAgencyDataSource.all();
            updateList(mAgencies);
        }
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_agencies;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.agencies, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search:
                startActivity(new Intent(this, SearchActivity.class));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected Callback<List<Agency>> mAgenciesCallback = new Callback<List<Agency>>() {
        @Override
        public void success(List<Agency> agencies, Response response) {
            mAgencyDataSource.sync(agencies);
            saveLastSync();
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

    protected boolean needSync() {
        SharedPreferences preferences = getSharedPreferences("agencies", Context.MODE_PRIVATE);
        String lastsync = preferences.getString("lastsync", null);

        if (lastsync == null) {
            return true;
        }

        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
        LocalDate lastTime = formatter.parseDateTime(lastsync).toLocalDate();
        LocalDate currentTime = new DateTime().toLocalDate();

        int daysDiff = Days.daysBetween(currentTime, lastTime).getDays();

        return daysDiff > 1;
    }

    protected void saveLastSync() {
        String lastsync = new DateTime().toLocalDate().toString();
        SharedPreferences preferences = getSharedPreferences("agencies", Context.MODE_PRIVATE);
        preferences.edit().putString("lastsync", lastsync).commit();
    }

}
