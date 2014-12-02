package com.socialimprover.saldotuc.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.socialimprover.saldotuc.models.Card;
import com.socialimprover.saldotuc.provider.SaldoTucContract.CardsColumns;
import com.socialimprover.saldotuc.provider.SaldoTucHelper.Tables;

import java.util.ArrayList;
import java.util.List;

public class CardDataSource {

    private SaldoTucHelper mSaldoTucHelper; // Helper class for creating and opening the DB

    public CardDataSource(Context context) {
        mSaldoTucHelper = new SaldoTucHelper(context);
    }

    private SQLiteDatabase open() {
        return mSaldoTucHelper.getWritableDatabase();
    }

    private void close(SQLiteDatabase database) {
        if (database != null) {
            database.close();
        }
    }

    private int getIntFromColumnName(Cursor cursor, String columnName) {
        int columnIndex = cursor.getColumnIndex(columnName);
        return cursor.getInt(columnIndex);
    }

    private String getStringFromColumnName(Cursor cursor, String columnName) {
        int columnIndex = cursor.getColumnIndex(columnName);
        return cursor.getString(columnIndex);
    }

     /*
     * CRUD operations!
     */
    public List<Card> all() {
        SQLiteDatabase database = open();
        ArrayList<Card> cards = new ArrayList<Card>();
        Cursor cursor = database.rawQuery("SELECT * FROM " + Tables.CARDS + " ORDER BY LOWER(" + CardsColumns.CARD_NAME + ")", null);

        if (cursor.moveToFirst()) {
            do {
                Card card = new Card(getIntFromColumnName(cursor, CardsColumns.CARD_ID),
                        getStringFromColumnName(cursor, CardsColumns.CARD_NAME),
                        getStringFromColumnName(cursor, CardsColumns.CARD_CARD),
                        getStringFromColumnName(cursor, CardsColumns.CARD_LAST_BALANCE));

                cards.add(card);
            } while (cursor.moveToNext());
        }

        cursor.close();
        close(database);

        return cards;
    }

    public void create(Card card) {
        SQLiteDatabase database = open();

        ContentValues values = new ContentValues();
        values.put(CardsColumns.CARD_NAME, card.getName());
        values.put(CardsColumns.CARD_CARD, card.getNumber());
        values.put(CardsColumns.CARD_PHONE, card.getPhone());
        values.put(CardsColumns.CARD_HOUR, card.getHour());
        values.put(CardsColumns.CARD_AMPM, card.getAmpm());
        values.put(CardsColumns.CARD_LAST_BALANCE, card.getBalance());

        database.insert(Tables.CARDS, null, values);

        close(database);
    }

    public Card findByNumber(String cardNumber) {
        SQLiteDatabase database = open();
        Cursor cursor = database.rawQuery("SELECT * FROM " + Tables.CARDS + " WHERE " + CardsColumns.CARD_CARD + " = '" + cardNumber + "'", null);
        Card card = null;

        if (cursor.moveToFirst()) {
            card = new Card(getIntFromColumnName(cursor, CardsColumns.CARD_ID),
                    getStringFromColumnName(cursor, CardsColumns.CARD_NAME),
                    getStringFromColumnName(cursor, CardsColumns.CARD_CARD),
                    getStringFromColumnName(cursor, CardsColumns.CARD_LAST_BALANCE));
        }

        close(database);

        if (card != null) {
            return card;
        }

        return null;
    }

    public void update(Card card) {
        SQLiteDatabase database = open();

        ContentValues values = new ContentValues();
        values.put(CardsColumns.CARD_NAME, card.getName());
        values.put(CardsColumns.CARD_CARD, card.getNumber());
        values.put(CardsColumns.CARD_PHONE, card.getPhone());
        values.put(CardsColumns.CARD_HOUR, card.getHour());
        values.put(CardsColumns.CARD_AMPM, card.getAmpm());
        values.put(CardsColumns.CARD_LAST_BALANCE, card.getBalance());

        database.update(Tables.CARDS,
                values,
                String.format("%s=%d", CardsColumns.CARD_ID, card.getId()), null);

        close(database);
    }

    public int delete(Card card) {
        SQLiteDatabase database = open();

        return database.delete(Tables.CARDS,
                String.format("%s=%s", CardsColumns.CARD_ID, String.valueOf(card.getId())),
                null);
    }

}
