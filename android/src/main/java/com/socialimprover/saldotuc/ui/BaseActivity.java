package com.socialimprover.saldotuc.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.socialimprover.saldotuc.R;
import com.socialimprover.saldotuc.model.Card;
import com.socialimprover.saldotuc.provider.CardDataSource;
import com.socialimprover.saldotuc.util.AnalyticsManager;
import com.socialimprover.saldotuc.util.PrefUtils;

import java.util.List;

import static com.socialimprover.saldotuc.util.LogUtils.makeLogTag;

public class BaseActivity extends AppCompatActivity {
    public static final String TAG = makeLogTag("BaseActivity");
    private Toolbar mActionBarToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AnalyticsManager.initializeAnalyticsTracker(getApplicationContext());
        ActionBar ab = getSupportActionBar();

        if (!PrefUtils.isDataImportedDone(this)) {
            try {
                CardDataSource cardDataSource = new CardDataSource(this);
                List<Card> cards = cardDataSource.all();

                if (!cards.isEmpty()) {
                    Card.pinAllInBackground(cards);
                    cardDataSource.dropTables();
                }

                PrefUtils.markDataImportedDone(this);
            } catch (Exception ignored) {}
        }

        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onDestroy() {
        MixpanelAPI mixpanel = AnalyticsManager.getTracker();
        if (mixpanel != null) {
            mixpanel.flush();
        }
        super.onDestroy();
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        getActionBarToolbar();
    }

    protected Toolbar getActionBarToolbar() {
        if (mActionBarToolbar == null) {
            mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);

            if (mActionBarToolbar != null) {
                setSupportActionBar(mActionBarToolbar);
            }
        }

        return mActionBarToolbar;
    }
}
