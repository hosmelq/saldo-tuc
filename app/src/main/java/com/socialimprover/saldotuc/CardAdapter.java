package com.socialimprover.saldotuc;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.socialimprover.saldotuc.app.R;

import java.util.List;

public class CardAdapter extends ArrayAdapter<Card> {

    public static final String TAG = CardAdapter.class.getSimpleName();

    protected Context mContext;
    protected List<Card> mCards;

    public CardAdapter(Context context, List<Card> cards) {
        super(context, R.layout.card_item, cards);
        mContext = context;
        mCards = cards;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.card_item, null);
            holder = new ViewHolder();
            holder.nameLabel = (TextView) convertView.findViewById(R.id.cardName);
            holder.numberLabel = (TextView) convertView.findViewById(R.id.cardNumber);
            holder.balanceLabel = (TextView) convertView.findViewById(R.id.cardBalance);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Card card = mCards.get(position);
        String number = AppUtil.formatCard(card.getNumber());
        String balance;

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

    private static class ViewHolder {
        TextView nameLabel;
        TextView numberLabel;
        TextView balanceLabel;
    }

    public void refill(List<Card> cards) {
        mCards.clear();
        mCards.addAll(cards);
        notifyDataSetChanged();
    }
}
