package com.socialimprover.saldotuc.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

public class SaldoTucDataSource {

    private SQLiteDatabase mDatabase; // The actual DB!
    private SaldoTucHelper mSaldoTucHelper; // Helper class for creating and opening the DB
    private Context mContext;

    public SaldoTucDataSource(Context context) {
        mContext = context;
        mSaldoTucHelper = new SaldoTucHelper(mContext);
    }

    /*
     * Open the db. Will create if it doesn't exist
     */
    public void open() throws SQLiteException {
        mDatabase = mSaldoTucHelper.getWritableDatabase();
    }

    /*
     * We always need to close our db connections
     */
    public void close() {
        mDatabase.close();
    }

     /*
     * CRUD operations!
     */
    public void insertCard(Card card) {
        ContentValues values = new ContentValues();
        values.put(SaldoTucHelper.COLUMN_NAME, card.getName());
        values.put(SaldoTucHelper.COLUMN_CARD, card.getCard());
        values.put(SaldoTucHelper.COLUMN_PHONE, card.getPhone());
        values.put(SaldoTucHelper.COLUMN_HOUR, card.getHour());
        values.put(SaldoTucHelper.COLUMN_AMPM, card.getAmpm());

        if (card.getBalance() != null) {
            values.put(SaldoTucHelper.COLUMN_LAST_BALANCE, card.getBalance());
        }

        mDatabase.insert(SaldoTucHelper.TABLE_CARDS, null, values);
    }

    public Cursor selectAllCards() {
        return mDatabase.rawQuery("SELECT * FROM cards", null);
    }

    public Cursor selectCard(Integer id) {
        Cursor cursor = mDatabase.query(
            SaldoTucHelper.TABLE_CARDS, // table
            new String[] { SaldoTucHelper.COLUMN_CARD, SaldoTucHelper.COLUMN_LAST_BALANCE, SaldoTucHelper.COLUMN_NAME }, // column names
            SaldoTucHelper.COLUMN_ID + " = ?", // where clause
            new String[] { String.valueOf(id) }, // where params
            null, // groupby
            null, // having
            null  // orderby
        );

        return cursor;
    }

}
