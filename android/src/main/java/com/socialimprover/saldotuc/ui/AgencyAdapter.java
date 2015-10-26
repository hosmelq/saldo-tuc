package com.socialimprover.saldotuc.ui;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.socialimprover.saldotuc.R;
import com.socialimprover.saldotuc.api.model.Agency;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.socialimprover.saldotuc.util.LogUtils.makeLogTag;

public class AgencyAdapter extends RecyclerView.Adapter<ViewHolder> {

    public static final String TAG = makeLogTag("AgencyAdapter");
    private static final int ITEM_TYPE_ITEM = 1;
    private static final int ITEM_TYPE_GROUP = 2;
    private List<Agency> mAgencies;

    public AgencyAdapter(List<Agency> agencies) {
        mAgencies = agencies;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

        switch (viewType) {
            case ITEM_TYPE_GROUP:
                view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_group_agency, parent, false);
                return new NeighborhoodHolder(view);

            case ITEM_TYPE_ITEM:
                view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_agency, parent, false);
                return new AgencyHolder(view);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case ITEM_TYPE_GROUP:
                NeighborhoodHolder neighborhoodHolder = (NeighborhoodHolder) holder;
                neighborhoodHolder.bindNeighborhood(mAgencies.get(position));
                break;

            case ITEM_TYPE_ITEM:
                AgencyHolder agencyHolder = (AgencyHolder) holder;
                agencyHolder.bindAgency(mAgencies.get(position));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mAgencies == null ? 0 : mAgencies.size();
    }

    @Override
    public int getItemViewType(int position) {
        Agency agency = mAgencies.get(position);

        if (agency.address == null) {
            return ITEM_TYPE_GROUP;
        }

        return ITEM_TYPE_ITEM;
    }

    public class NeighborhoodHolder extends RecyclerView.ViewHolder {

        public final String TAG = makeLogTag("NeighborhoodHolder");

        @Bind(R.id.nameView) TextView mNameView;

        public NeighborhoodHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bindNeighborhood(Agency agency) {
            mNameView.setText(agency.name);
        }
    }

    public class AgencyHolder extends RecyclerView.ViewHolder {

        public final String TAG = makeLogTag("AgencyHolder");

        @Bind(R.id.nameView) TextView mNameView;
        @Bind(R.id.addressView) TextView mAddressView;

        public AgencyHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bindAgency(Agency agency) {
            mNameView.setText(agency.name);
            mAddressView.setText(agency.address);
        }
    }
}
