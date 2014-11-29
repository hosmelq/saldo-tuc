package com.socialimprover.saldotuc.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.socialimprover.saldotuc.app.R;
import com.socialimprover.saldotuc.models.District;

import java.util.List;

public class DistrictAdapter extends ArrayAdapter<District> {

    public static final String TAG = DistrictAdapter.class.getSimpleName();

    protected Context mContext;
    protected List<District> mDistricts;

    public DistrictAdapter(Context context, List<District> districts) {
        super(context, R.layout.district_item, districts);
        mContext = context;
        mDistricts = districts;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        District district = mDistricts.get(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.district_item, null);
            holder = new ViewHolder();
            holder.nameLabel = (TextView) convertView.findViewById(R.id.nameLabel);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.nameLabel.setText(district.getName());

        return convertView;
    }

    public void refill(List<District> districts) {
        mDistricts.clear();
        mDistricts.addAll(districts);
        notifyDataSetChanged();
    }

    private static class ViewHolder {
        TextView nameLabel;
    }

}