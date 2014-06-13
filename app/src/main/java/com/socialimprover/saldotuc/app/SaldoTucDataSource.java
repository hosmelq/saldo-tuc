package com.socialimprover.saldotuc.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

public class SaldoTucDataSource {

    private SQLiteDatabase mDatabase; // The actual DB!
    private SaldoTucHelper mSaldoTucHelper; // Helper class for creating and opening the DB

    public SaldoTucDataSource(Context context) {
        mSaldoTucHelper = new SaldoTucHelper(context);
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
        return mDatabase.rawQuery("SELECT * FROM " + SaldoTucHelper.TABLE_CARDS, null);
    }

    public Cursor selectCard(Integer id) {
        return mDatabase.query(
            SaldoTucHelper.TABLE_CARDS, // table
            new String[]{SaldoTucHelper.COLUMN_CARD, SaldoTucHelper.COLUMN_LAST_BALANCE, SaldoTucHelper.COLUMN_NAME}, // column names
            SaldoTucHelper.COLUMN_ID + " = ?", // where clause
            new String[]{String.valueOf(id)}, // where params
            null, // groupby
            null, // having
            null  // orderby
        );
    }

    public int updateCard(Integer id, String balance) {
        ContentValues values = new ContentValues();
        values.put(SaldoTucHelper.COLUMN_LAST_BALANCE, balance);

        return mDatabase.update(
            SaldoTucHelper.TABLE_CARDS, // table
            values, // values
            SaldoTucHelper.COLUMN_ID + " = ?",   // where clause
            new String[] { id.toString() } // where params
        );

//        String query = "UPDATE " + SaldoTucHelper.TABLE_CARDS + " SET " + SaldoTucHelper.COLUMN_LAST_BALANCE + " = \"" + balance + "\" WHERE " + SaldoTucHelper.COLUMN_ID + " = " + id;
//        Log.e("TAG", query);
//        mDatabase.rawQuery(query, null);
    }

}
