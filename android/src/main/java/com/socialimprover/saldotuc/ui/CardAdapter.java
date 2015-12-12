package com.socialimprover.saldotuc.ui;

import android.content.Context;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParsePush;
import com.socialimprover.saldotuc.Config;
import com.socialimprover.saldotuc.R;
import com.socialimprover.saldotuc.api.MpesoService;
import com.socialimprover.saldotuc.api.SaldoTucService;
import com.socialimprover.saldotuc.api.ServiceFactory;
import com.socialimprover.saldotuc.exceptions.InvalidCardException;
import com.socialimprover.saldotuc.model.Card;
import com.socialimprover.saldotuc.util.AnalyticsManager;
import com.socialimprover.saldotuc.util.AppUtil;
import com.socialimprover.saldotuc.util.SyncHelper;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.socialimprover.saldotuc.util.LogUtils.makeLogTag;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardHolder> implements FindCallback<Card> {

    public static final String TAG = makeLogTag("CardAdapter");
    public static final int minBalance = 10;
    private Callbacks mCallbacks;
    private List<Card> mCards;

    public interface Callbacks {
        void onCardEdit(Card card);

        void onShowChart(Card card);
    }

    public CardAdapter(Callbacks activity) {
        mCallbacks = activity;
    }

    @Override
    public CardHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.list_item_card, parent, false);

        return new CardHolder(view, parent.getContext());
    }

    @Override
    public void onBindViewHolder(CardHolder holder, int position) {
        holder.bindCard(mCards.get(position));
    }

    @Override
    public int getItemCount() {
        return mCards == null ? 0 : mCards.size();
    }

    @Override
    public void done(List<Card> cards, ParseException e) {
        if (e == null) {
            mCards = cards;
            notifyDataSetChanged();
        }
    }

    public class CardHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final String TAG = makeLogTag("CardHolder");
        private final static String SCREEN_LABEL = "Cards";
        private Context mContext;
        private Card mCard;
        private boolean isFetching;

        @Bind(R.id.nameView) TextView mNameView;
        @Bind(R.id.numberView) TextView mNumberView;
        @Bind(R.id.balanceView) TextView mBalanceView;
        @Bind(R.id.actionControls) LinearLayout mActionControls;
        @Bind(R.id.progressBar) ProgressBar mProgressBar;

        public CardHolder(View itemView, Context context) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);

            mContext = context;
        }

        @Override
        public void onClick(View v) {
            toggleActions();
        }

        @OnClick(R.id.checkBalanceButton)
        protected void onMenuItemClick(View v) {
            if (SyncHelper.isOnline(mContext)) {
                getBalance(getAdapterPosition());
            } else {
                //noinspection ResourceType
                Snackbar.make(itemView, mContext.getString(R.string.no_connection), Config.SNACKBAR_LONG_DURATION_MS).show();
            }
        }

        @OnClick(R.id.showGraphButton)
        protected void onShowGraphItemClick(View v) {
            showChart();
        }

        @OnClick(R.id.editButton)
        protected void onEditItemClick(View v) {
            editCard();
        }

        @OnClick(R.id.deleteButton)
        protected void onDeleteItemClick(View v) {
            deleteCard(getAdapterPosition());
        }

        private void bindCard(Card card) {
            mCard = card;

            if (card.getNotifications()) {
                mNameView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_notifications_black_16dp, 0, 0, 0);
            } else {
                mNameView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            }

            mNameView.setText(card.getName());
            mNumberView.setText(card.getNumberFormatted());
            setBalance(card.getBalance());
        }

        private void setBalance(String balance) {
            if (balance == null || balance.isEmpty()) {
                mBalanceView.setText("");
                return;
            }

            mBalanceView.setText(String.format(" • C$ %s", balance));

            if (Double.parseDouble(balance) <= minBalance) {
                mBalanceView.setTextColor(Color.RED);
            } else {
                mBalanceView.setTextColor(Color.parseColor("#636363"));
            }
        }

        private void hideLoading() {
            isFetching = false;
            mProgressBar.setVisibility(View.GONE);
            mProgressBar.setIndeterminate(false);
        }

        private void showLoading() {
            isFetching = true;
            mProgressBar.setVisibility(View.VISIBLE);
            mProgressBar.setIndeterminate(true);
        }

        private boolean actionsAreOpen() {
            return mActionControls.getVisibility() == View.VISIBLE;
        }

        private void toggleActions() {
            if (actionsAreOpen()) {
                hideActions();
            } else {
                showActions();
            }
        }

        private void hideActions() {
            mActionControls.setVisibility(View.GONE);
        }

        private void showActions() {
            mActionControls.setVisibility(View.VISIBLE);
        }

        private void getBalance(int position) {
            if (isFetching) {
                return;
            }

            MpesoService mpesoService = ServiceFactory.createRetrofitService(MpesoService.class, MpesoService.SERVICE_ENDPOINT);
            SaldoTucService saldoTucService = ServiceFactory.createRetrofitService(SaldoTucService.class, SaldoTucService.SERVICE_ENDPOINT);

            showLoading();
            AnalyticsManager.timeEvent(Config.MIXPANEL_REQUEST_BALANCE_EVENT);
            mpesoService.getBalance("1", mCard.getNumber())
                .flatMap(mpesoCard -> {
                    String balance = AppUtil.parseBalance(mpesoCard.Mensaje);

                    if (balance == null) {
                        return Observable.error(new InvalidCardException("Not a TUC card number or inactive"));
                    }

                    /* [ANALYTICS:EVENT]
                    * TRIGGER:  Request card's balance.
                    * CATEGORY: 'Cards'
                    * ACTION:   'Request Balance'
                    * LABEL:    card balance
                    * [/ANALYTICS]
                    */
                    AnalyticsManager.sendEvent(SCREEN_LABEL, Config.MIXPANEL_REQUEST_BALANCE_EVENT, balance);
                    mCard.setBalance(balance);
                    mCard.pinInBackground();

                    return saldoTucService.storeBalance(mCard.getNumber(), balance);
                })
                .subscribeOn(Schedulers.newThread())
                .unsubscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(balance -> {
                    hideLoading();
                    notifyItemChanged(position);
                }, throwable -> {
                    hideLoading();

                    if (throwable instanceof InvalidCardException) {
                        //noinspection ResourceType
                        Snackbar.make(itemView, mContext.getString(R.string.card_invalid_error), Config.SNACKBAR_LONG_DURATION_MS).show();
                    } else {
                        notifyItemChanged(position);
                    }
                });
        }

        private void showChart() {
            mCallbacks.onShowChart(mCard);
        }

        private void editCard() {
            mCallbacks.onCardEdit(mCard);
        }

        private void deleteCard(int position) {
            mCards.remove(position);
            notifyItemRemoved(position);

            //noinspection ResourceType
            Snackbar.make(itemView, mContext.getString(R.string.card_deleted, mCard.getNumberFormatted()), Config.SNACKBAR_LONG_DURATION_MS)
                .setAction(R.string.undo, v -> {
                    mCards.add(position, mCard);
                    notifyItemInserted(position);
                })
                .setCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        if (Snackbar.Callback.DISMISS_EVENT_ACTION != event && Snackbar.Callback.DISMISS_EVENT_MANUAL != event) {
                            mCard.unpinInBackground();
                            ParsePush.unsubscribeInBackground(mCard.getChannelName());
                        }
                    }
                })
                .show();
        }
    }
}
