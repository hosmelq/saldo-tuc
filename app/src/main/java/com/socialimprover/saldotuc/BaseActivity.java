package com.socialimprover.saldotuc;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ProgressBar;

import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.socialimprover.saldotuc.app.R;

import org.json.JSONObject;

public abstract class BaseActivity extends ActionBarActivity {

    private MixpanelAPI mMixpanel;
    private Toolbar mToolbar;
    private ProgressBar mToolbarProgressbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResource());

        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            mToolbar.setTitleTextColor(getResources().getColor(android.R.color.white));

            mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white);

            mToolbarProgressbar = (ProgressBar) findViewById(R.id.toolbar_progress_bar);
        }

        mMixpanel = SaldoTucApplication.getMixpanelInstance(this);
    }

    @Override
    protected void onDestroy() {
        mMixpanel.flush();
        super.onDestroy();
    }

    protected abstract int getLayoutResource();

    protected void setActionBarIcon(int iconRes) {
        mToolbar.setNavigationIcon(iconRes);
    }

    protected void setActionBarTitle(int stringRes) {
        getSupportActionBar().setTitle(stringRes);
    }

    protected void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    protected void showProgressBar() {
        if (mToolbarProgressbar != null) {
            mToolbarProgressbar.setVisibility(ProgressBar.VISIBLE);
        }
    }

    protected void hideProgressBar() {
        if (mToolbarProgressbar != null) {
            mToolbarProgressbar.setVisibility(ProgressBar.GONE);
        }
    }

    protected void mixpanelTrackEvent(String event, String identify, JSONObject props) {
        if (identify != null) {
            mMixpanel.identify(identify);
        }

        mMixpanel.track(event, props);
    }

}
