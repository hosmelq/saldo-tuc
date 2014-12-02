package com.socialimprover.saldotuc.ui;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.socialimprover.saldotuc.app.R;
import com.socialimprover.saldotuc.models.Agency;
import com.socialimprover.saldotuc.provider.AgencyDataSource;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends BaseActivity {

    public static final String TAG = SearchActivity.class.getSimpleName();

    protected AgencyDataSource mAgencyDataSource;
    protected ListView mListView;
    protected List<Agency> mAgencies;
    protected List<Agency> mAgenciesFiltered;
    protected SearchView mSearchView = null;
    protected String mQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAgencyDataSource = new AgencyDataSource(this);

        Toolbar toolbar = getToolbar();

        setActionBarTitle(getString(R.string.title_activity_search));
        toolbar.setBackgroundColor(Color.WHITE);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_grey600);

        mListView = (ListView) findViewById(android.R.id.list);
        mListView.setOnItemClickListener(mOnItemClickListener);

        String query = getIntent().getStringExtra(SearchManager.QUERY);
        query = query == null ? "" : query;
        mQuery = query;
        mAgencies = mAgencyDataSource.all();

        if (mSearchView != null) {
            mSearchView.setQuery(query, false);
        }
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_search;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);
        final MenuItem searchItem = menu.findItem(R.id.search);

        if (searchItem != null) {
            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            final SearchView view = (SearchView) MenuItemCompat.getActionView(searchItem);
            mSearchView = view;

            if (view == null) {
                Log.e(TAG, "Could not set up search view, view is null.");
            } else {
                view.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
                view.setIconified(false);

                // query text listeners
                view.setOnQueryTextListener(searchOnQueryTextListener);

                view.setOnCloseListener(new SearchView.OnCloseListener() {
                    @Override
                    public boolean onClose() {
                        finish();
                        return false;
                    }
                });
            }

            if (view != null && ! TextUtils.isEmpty(mQuery)) {
                view.setQuery(mQuery, false);
            }
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.search) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected SearchView.OnQueryTextListener searchOnQueryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String s) {
            filterAgenciesList(mAgencies, s);
            return true;
        }

        @Override
        public boolean onQueryTextChange(String s) {
            filterAgenciesList(mAgencies, s);
            return true;
        }
    };

    protected AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            Agency agency = mAgenciesFiltered.get(position);

            Intent intent = new Intent(SearchActivity.this, AgencyActivity.class);
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

    protected void filterAgenciesList(List<Agency> agencies, String filter) {
        List<Agency> list = new ArrayList<Agency>();

        clearAdapter();

        if (agencies != null && ! TextUtils.isEmpty(filter)) {
            for (Agency agency : agencies) {
                if (agency.getNeighborhood().toLowerCase().contains(filter.toLowerCase())) {
                    list.add(agency);
                }
            }

            mAgenciesFiltered = list;
            updateList(list);
        }
    }

    private List<Agency> cloneAgencies(List<Agency> agencies) {
        List<Agency> clone = new ArrayList<Agency>(agencies.size());

        for (Agency agency : agencies) {
            clone.add(new Agency(agency));
        }

        return clone;
    }

    public void clearAdapter() {
        if (mListView.getAdapter() != null) {
            ( (AgencyAdapter) mListView.getAdapter()).clear();
        }
    }
}
