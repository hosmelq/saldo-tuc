package com.socialimprover.saldotuc.ui;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.socialimprover.saldotuc.app.R;
import com.socialimprover.saldotuc.models.Card;
import com.socialimprover.saldotuc.util.AppUtil;

import java.util.List;

public class CardAdapter extends ArrayAdapter<Card> {

    public static final String TAG = CardAdapter.class.getSimpleName();

    protected Context mContext;
    protected List<Card> mCards;
    protected final int VIEW_TYPE_SEPARATOR = 0;
    protected final int VIEW_TYPE_ITEM = 1;

    public CardAdapter(Context context, List<Card> cards) {
        super(context, R.layout.card_item, cards);
        mContext = context;
        mCards = cards;
    }

    @Override
    public int getItemViewType(int position) {
        String previousCardChar = position == 0 ? null : mCards.get(position - 1).getName().substring(0, 1).toLowerCase();
        String currentCardChar = mCards.get(position).getName().substring(0, 1).toLowerCase();

        if (previousCardChar == null || ! previousCardChar.equals(currentCardChar)) {
            return VIEW_TYPE_SEPARATOR;
        }

        return VIEW_TYPE_ITEM;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String balance;
        ViewHolder holder;

        Card card = mCards.get(position);
        String number = AppUtil.formatCard(card.getNumber());
        int type = getItemViewType(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.card_item, null);
            holder = new ViewHolder();
            holder.chartLabel = (TextView) convertView.findViewById(R.id.charLabel);
            holder.nameLabel = (TextView) convertView.findViewById(R.id.cardName);
            holder.numberLabel = (TextView) convertView.findViewById(R.id.cardNumber);
            holder.balanceLabel = (TextView) convertView.findViewById(R.id.cardBalance);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        switch (type) {
            case VIEW_TYPE_SEPARATOR:
                holder.chartLabel.setText(card.getName().substring(0, 1).toUpperCase());
                holder.chartLabel.setVisibility(TextView.VISIBLE);
                break;
            case VIEW_TYPE_ITEM:
                holder.chartLabel.setVisibility(TextView.GONE);
                break;
        }

        holder.nameLabel.setText(card.getName());
        holder.numberLabel.setText(number);

        if (card.getBalance() == null || TextUtils.isEmpty(card.getBalance())) {
            balance = "";
        } else {
            balance = "C$ " + card.getBalance();
        }

        holder.balanceLabel.setText(balance);

        return convertView;
    }

    public void refill(List<Card> cards) {
        mCards.clear();
        mCards.addAll(cards);
        notifyDataSetChanged();
    }

    private static class ViewHolder {
        TextView chartLabel;
        TextView nameLabel;
        TextView numberLabel;
        TextView balanceLabel;
    }

}
