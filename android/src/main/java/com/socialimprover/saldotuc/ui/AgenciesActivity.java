package com.socialimprover.saldotuc.ui;

import android.content.Context;
import android.os.Bundle;
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

import com.socialimprover.saldotuc.R;
import com.socialimprover.saldotuc.api.SaldoTucService;
import com.socialimprover.saldotuc.api.ServiceFactory;
import com.socialimprover.saldotuc.api.model.Agency;
import com.socialimprover.saldotuc.api.model.Neighborhood;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.socialimprover.saldotuc.util.LogUtils.LOGD;
import static com.socialimprover.saldotuc.util.LogUtils.makeLogTag;

public class AgenciesActivity extends BaseActivity {
    public static final String TAG = makeLogTag("AgenciesActivity");
    Toolbar mToolbar;
    List<Neighborhood> mNeighborhoods;

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

                if (s.length() > 0 && mNeighborhoods != null) {
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
    }

    @Override
    public void onBackPressed() {
        if (mSearchBar.getVisibility() == View.VISIBLE) {
            hideSearchBar();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.agencies, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_search) {
            showSearchBar();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.arrowBack)
    protected void onArrowBackClick() {
        hideSearchBar();
    }

    private void hideSearchBar() {
        mToolbar.setVisibility(View.VISIBLE);
        mSearchBar.setVisibility(View.GONE);
        mSearchInput.setText("");
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && imm.isAcceptingText()) {
            imm.toggleSoftInput(0, InputMethodManager.HIDE_IMPLICIT_ONLY);
        }
        //check
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

    private void hideLoading() {
        mProgressBar.setVisibility(View.GONE);
        mProgressBar.setIndeterminate(false);
    }

    private void showLoading() {
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressBar.setIndeterminate(true);
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
        AgencyAdapter cardAdapter = new AgencyAdapter(agencies);
        mRecyclerView.swapAdapter(cardAdapter, true);
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
}
