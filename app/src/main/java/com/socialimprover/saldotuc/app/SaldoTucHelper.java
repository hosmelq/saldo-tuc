package com.socialimprover.saldotuc.app;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SaldoTucHelper extends SQLiteOpenHelper {

    // Database information
    private static final String DB_NAME = "mpeso";
    private static final int DB_VERSION = 1;

    // Table and column information
    public static final String TABLE_CARDS = "CARDS";
    public static final String COLUMN_ID = "_ID";
    public static final String COLUMN_NAME = "NAME";
    public static final String COLUMN_CARD = "CARD";
    public static final String COLUMN_PHONE = "PHONE";
    public static final String COLUMN_HOUR = "HOUR";
    public static final String COLUMN_AMPM = "AMPM";
    public static final String COLUMN_LAST_BALANCE = "LAST_BALANCE";

    public SaldoTucHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
            "CREATE TABLE " + TABLE_CARDS +
            " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_NAME + " TEXT, " +
            COLUMN_CARD + " TEXT, " +
            COLUMN_PHONE + " TEXT, " +
            COLUMN_HOUR + " TEXT, " +
            COLUMN_AMPM + " TEXT, " +
            COLUMN_LAST_BALANCE + " INTEGER)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i2) {
    }
}
