package com.socialimprover.saldotuc.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;
import com.socialimprover.saldotuc.Config;
import com.socialimprover.saldotuc.R;
import com.socialimprover.saldotuc.api.SaldoTucService;
import com.socialimprover.saldotuc.api.ServiceFactory;
import com.socialimprover.saldotuc.api.model.Agency;
import com.socialimprover.saldotuc.api.model.Neighborhood;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.socialimprover.saldotuc.util.LogUtils.LOGD;
import static com.socialimprover.saldotuc.util.LogUtils.makeLogTag;

public class AgenciesActivity extends BaseActivity
    implements AgencyAdapter.Callbacks, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    public static final String TAG = makeLogTag("AgenciesActivity");
    public static final int REQUEST_ACCESS_FINE_LOCATION = 1;
    private Toolbar mToolbar;
    private MenuItem mNearbyAgenciesMenuItem;
    private MenuItem mSearchMenuItem;
    private GoogleApiClient mGoogleApiClient;
    private List<Neighborhood> mNeighborhoods = new ArrayList<>();
    private Location mLocation;
    private boolean mWaitingForLocation = false;

    @Bind(R.id.searchBar) LinearLayout mSearchBar;
    @Bind(R.id.searchInput) EditText mSearchInput;
    @Bind(R.id.recyclerView) RecyclerView mRecyclerView;
    @Bind(R.id.progressBar) ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agencies);
        ButterKnife.bind(this);

        mToolbar = getActionBarToolbar();
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        mToolbar.setNavigationOnClickListener(view -> finish());

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mSearchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                List<Neighborhood> filterList = new ArrayList<>();

                if (s.length() > 0) {
                    for (Neighborhood neighborhood : mNeighborhoods) {
                        if (neighborhood.name != null && neighborhood.name.toLowerCase().contains(s.toString().trim().toLowerCase())) {
                            filterList.add(neighborhood);
                        }
                    }
                } else {
                    filterList.clear();
                }

                updateAdapter(filterList);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        fetchAgencies();

        if (hasGps()) {
            requestForPermissions();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onBackPressed() {
        if (mSearchBar.getVisibility() == View.VISIBLE) {
            hideSearchBar();
        } else if (mToolbar.getTitle().equals(getString(R.string.nearby_agencies))) {
            removeNearbyAgenciesToolbar();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.agencies, menu);
        mNearbyAgenciesMenuItem = menu.findItem(R.id.action_nearby_agencies);
        mSearchMenuItem = menu.findItem(R.id.action_search);

        if (!checkIfWeHavePermissions()) {
            mNearbyAgenciesMenuItem.setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_nearby_agencies:
                showNearbyAgencies();
                return true;

            case R.id.action_search:
                showSearchBar();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_ACCESS_FINE_LOCATION:
                if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setGoogleClient();
                    mNearbyAgenciesMenuItem.setVisible(true);
                } else {
                    LOGD(TAG, "boo! no showMap");
                }

                break;
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (location != null) {
            mLocation = location;

            if (mWaitingForLocation) {
                mWaitingForLocation = false;
                showNearbyAgencies();
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    @Override
    public void onShowAgency(Agency agency) {
        Intent intent = new Intent(this, AgencyActivity.class);
        intent.putExtra("agency", agency);
        startActivity(intent);
    }

    @OnClick(R.id.arrowBack)
    protected void onArrowBackClick() {
        hideSearchBar();
    }

    private void requestForPermissions() {
        if (!checkIfWeHavePermissions()) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            } else {
                ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_ACCESS_FINE_LOCATION);
            }
        } else {
            setGoogleClient();
        }
    }

    private boolean checkIfWeHavePermissions() {
        return ContextCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasGps() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
    }

    private boolean hasGpsEnable() {
        LocationManager manager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void setGoogleClient() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
            mGoogleApiClient.connect();
        } else {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        }
    }

    private void hideLoading() {
        mProgressBar.setVisibility(View.GONE);
        mProgressBar.setIndeterminate(false);
    }

    private void showLoading() {
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressBar.setIndeterminate(true);
    }

    private void hideSearchBar() {
        mToolbar.setVisibility(View.VISIBLE);
        mSearchBar.setVisibility(View.GONE);
        mSearchInput.setText("");
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && imm.isAcceptingText()) {
            imm.toggleSoftInput(0, InputMethodManager.HIDE_IMPLICIT_ONLY);
        }
        updateAdapter(mNeighborhoods);
    }

    private void showSearchBar() {
        mToolbar.setVisibility(View.GONE);
        mSearchBar.setVisibility(View.VISIBLE);
        mSearchInput.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
        }
        updateAdapter(new ArrayList<>());
    }

    private void showNearbyAgencies() {
        if (!hasGpsEnable()) {
            //noinspection ResourceType
            Snackbar.make(findViewById(R.id.container), getString(R.string.no_gps_enable), Config.SNACKBAR_LONG_DURATION_MS)
                .setAction(R.string.enable, v -> {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                })
                .show();
            return;
        }

        if (mLocation == null) {
            mWaitingForLocation = true;
            setGoogleClient();
            return;
        }

        LatLng currentLatLng = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
        List<Agency> agencies = getAgenciesWithLatLng();

        Collections.sort(agencies, (lhs, rhs) -> {
            double lDistance = SphericalUtil.computeDistanceBetween(currentLatLng, new LatLng(lhs.lat, lhs.lng));
            double rDistance = SphericalUtil.computeDistanceBetween(currentLatLng, new LatLng(rhs.lat, rhs.lng));

            return (lDistance > rDistance) ? 1 : -1;
        });

        setupNearbyAgenciesToolbar();
        swapAdapter(agencies);
    }

    private void fetchAgencies() {
        showLoading();
        SaldoTucService saldoTucService = ServiceFactory.createRetrofitService(SaldoTucService.class, SaldoTucService.SERVICE_ENDPOINT);

        saldoTucService.neighborhoods()
            .subscribeOn(Schedulers.newThread())
            .unsubscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(neighborhoods -> {
                hideLoading();
                mNeighborhoods = neighborhoods;
                updateAdapter(neighborhoods);
            }, throwable -> {
                hideLoading();
                LOGD(TAG, throwable.getMessage());
            });
    }

    private void updateAdapter(List<Neighborhood> neighborhoods) {
        List<Agency> agencies = flatAgencies(neighborhoods);

        swapAdapter(agencies);
    }

    private void swapAdapter(List<Agency> agencies) {
        mRecyclerView.swapAdapter(new AgencyAdapter(this, agencies), true);
    }

    public List<Agency> flatAgencies(List<Neighborhood> neighborhoods) {
        List<Agency> agencies = new ArrayList<>();

        for (Neighborhood neighborhood : neighborhoods) {
            Agency fakeAgency = new Agency();
            fakeAgency.name = neighborhood.name;
            agencies.add(fakeAgency);
            agencies.addAll(neighborhood.agencies);
        }

        return agencies;
    }

    public List<Agency> getAgenciesWithLatLng() {
        List<Agency> agencies = new ArrayList<>();

        for (Neighborhood neighborhood : mNeighborhoods) {
            //noinspection Convert2streamapi
            for (Agency agency : neighborhood.agencies) {
                if (agency.lat != 0 && agency.lng != 0) {
                    agencies.add(agency);
                }
            }
        }

        return agencies;
    }

    private void setupAllAgenciesToolbar() {
        mToolbar.setTitle(getString(R.string.title_activity_agencies));
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        mToolbar.setNavigationOnClickListener(view -> finish());
        mNearbyAgenciesMenuItem.setVisible(true);
        mSearchMenuItem.setVisible(true);
    }

    private void setupNearbyAgenciesToolbar() {
        mToolbar.setTitle(getString(R.string.nearby_agencies));
        mToolbar.setNavigationIcon(R.drawable.ic_close_white_24dp);
        mToolbar.setNavigationOnClickListener(view -> removeNearbyAgenciesToolbar());
        mNearbyAgenciesMenuItem.setVisible(false);
        mSearchMenuItem.setVisible(false);
    }

    private void removeNearbyAgenciesToolbar() {
        setupAllAgenciesToolbar();
        updateAdapter(mNeighborhoods);
    }
}
