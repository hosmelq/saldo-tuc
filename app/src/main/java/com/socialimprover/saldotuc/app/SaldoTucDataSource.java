package com.socialimprover.saldotuc.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

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
        values.put(SaldoTucHelper.COLUMN_CARD, card.getNumber());
        values.put(SaldoTucHelper.COLUMN_PHONE, card.getPhone());
        values.put(SaldoTucHelper.COLUMN_HOUR, card.getHour());
        values.put(SaldoTucHelper.COLUMN_AMPM, card.getAmpm());

        if (card.getBalance() != null) {
            values.put(SaldoTucHelper.COLUMN_LAST_BALANCE, card.getBalance());
        }

        mDatabase.insert(SaldoTucHelper.TABLE_CARDS, null, values);
    }

    public Cursor selectAllCards() {
        return mDatabase.rawQuery("SELECT * FROM " + SaldoTucHelper.TABLE_CARDS + " ORDER BY " + SaldoTucHelper.COLUMN_NAME, null);
    }

    public Cursor selectCard(Integer id) {
        return mDatabase.rawQuery("SELECT * FROM " + SaldoTucHelper.TABLE_CARDS + " WHERE " + SaldoTucHelper.COLUMN_ID + " = " + id, null);
    }

    public int updateCardBalance(Integer id, String balance) {
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
