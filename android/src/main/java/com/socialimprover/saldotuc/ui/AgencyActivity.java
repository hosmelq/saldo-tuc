package com.socialimprover.saldotuc.ui;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.socialimprover.saldotuc.R;
import com.socialimprover.saldotuc.api.model.Agency;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.socialimprover.saldotuc.util.LogUtils.makeLogTag;

public class AgencyActivity extends BaseActivity implements OnMapReadyCallback {
    public static final String TAG = makeLogTag("AgencyActivity");
    private Agency mAgency;

    @Bind(R.id.infoWrap) LinearLayout mInfoWrap;
    @Bind(R.id.mapWrap) FrameLayout mMapWrap;
    @Bind(R.id.nameView) TextView mNameView;
    @Bind(R.id.addressView) TextView mAddressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agency);
        ButterKnife.bind(this);

        mAgency = getIntent().getParcelableExtra("agency");

        final Toolbar toolbar = getActionBarToolbar();
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(view -> finish());
        new Handler().post(() -> toolbar.setTitle(mAgency.name));

        bindAgency(mAgency);

        if (mAgency.lat != 0 && mAgency.lng != 0) {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapview);
            mapFragment.getMapAsync(this);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mInfoWrap.setElevation(50);
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        LatLng latLng = new LatLng(mAgency.lat, mAgency.lng);

        map.addMarker(
            new MarkerOptions().position(latLng).title(mAgency.name)
        );
        map.moveCamera(
            CameraUpdateFactory.newLatLngZoom(latLng, 15)
        );

        mMapWrap.setVisibility(View.VISIBLE);
    }

    public void bindAgency(Agency agency) {
        mNameView.setText(agency.name);
        mAddressView.setText(agency.address);
    }
}
