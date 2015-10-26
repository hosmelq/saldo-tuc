package com.socialimprover.saldotuc.provider;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.socialimprover.saldotuc.model.Card;
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
        ArrayList<Card> cards = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM " + Tables.CARDS + " ORDER BY LOWER(" + CardsColumns.CARD_NAME + ")", null);

        if (cursor.moveToFirst()) {
            do {
                Card card = new Card();
                card.setBalance(getStringFromColumnName(cursor, CardsColumns.CARD_LAST_BALANCE));
                card.setName(getStringFromColumnName(cursor, CardsColumns.CARD_NAME));
                card.setNumber(getStringFromColumnName(cursor, CardsColumns.CARD_CARD));
                card.setNotifications(false);
                card.setUuid();

                cards.add(card);
            } while (cursor.moveToNext());
        }

        cursor.close();
        close(database);

        return cards;
    }

    public void dropTables() {
        SQLiteDatabase database = open();
        database.execSQL("DROP TABLE IF EXISTS " + Tables.CARDS);
        database.execSQL("DROP TABLE IF EXISTS " + Tables.AGENCIES);
        close(database);
    }
}
