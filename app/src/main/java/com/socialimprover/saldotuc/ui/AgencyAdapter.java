package com.socialimprover.saldotuc.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.socialimprover.saldotuc.app.R;
import com.socialimprover.saldotuc.models.Agency;

import java.util.List;

public class AgencyAdapter extends ArrayAdapter<Agency> {

    public static final String TAG = AgencyAdapter.class.getSimpleName();

    protected Context mContext;
    protected List<Agency> mAgencies;
    protected final int VIEW_TYPE_SEPARATOR = 0;
    protected final int VIEW_TYPE_ITEM = 1;

    public AgencyAdapter(Context context, List<Agency> agencies) {
        super(context, R.layout.agency_item, agencies);
        mContext = context;
        mAgencies = agencies;
    }

    @Override
    public int getItemViewType(int position) {
        String previousAgency = position == 0 ? null : mAgencies.get(position - 1).getNeighborhood().toLowerCase();
        String currentAgency = mAgencies.get(position).getNeighborhood().toLowerCase();

        if (previousAgency == null || ! previousAgency.equals(currentAgency)) {
            return VIEW_TYPE_SEPARATOR;
        }

        return VIEW_TYPE_ITEM;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        Agency agency = mAgencies.get(position);
        int type = getItemViewType(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.agency_item, null);
            holder = new ViewHolder();
            holder.neighborhoodLabel = (TextView) convertView.findViewById(R.id.neighborhoodLabel);
            holder.nameLabel = (TextView) convertView.findViewById(R.id.nameLabel);
            holder.addressLabel = (TextView) convertView.findViewById(R.id.addressLabel);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        switch (type) {
            case VIEW_TYPE_SEPARATOR:
                holder.neighborhoodLabel.setText(agency.getNeighborhood());
                holder.neighborhoodLabel.setVisibility(TextView.VISIBLE);
                break;
            case VIEW_TYPE_ITEM:
                holder.neighborhoodLabel.setVisibility(TextView.GONE);
                break;
        }

        holder.nameLabel.setText(agency.getName());
        holder.addressLabel.setText(agency.getAddress());

        return convertView;
    }

    public void refill(List<Agency> agencies) {
        mAgencies.clear();
        mAgencies.addAll(agencies);
        notifyDataSetChanged();
    }

    public void clear() {
        mAgencies.clear();
        notifyDataSetChanged();
    }

    private static class ViewHolder {
        TextView neighborhoodLabel;
        TextView nameLabel;
        TextView addressLabel;
    }

}
