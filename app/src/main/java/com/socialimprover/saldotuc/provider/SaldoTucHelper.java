package com.socialimprover.saldotuc.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.socialimprover.saldotuc.provider.SaldoTucContract.AgenciesColumns;
import com.socialimprover.saldotuc.provider.SaldoTucContract.CardsColumns;

public class SaldoTucHelper extends SQLiteOpenHelper {

    public static final String TAG = SaldoTucHelper.class.getSimpleName();

    private static final String DATABASE_NAME = "saldotuc";

    private static final int VER_2013_RELEASE_A = 1;
    private static final int VER_2014_RELEASE_B = 2;
    private static final int CUR_DATABASE_VERSION = VER_2014_RELEASE_B;

    interface Tables {
        String CARDS = "CARDS";
        String AGENCIES = "AGENCIES";
    }

    public SaldoTucHelper(Context context) {
        super(context, DATABASE_NAME, null, CUR_DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + Tables.CARDS + " ("
                + CardsColumns.CARD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + CardsColumns.CARD_NAME + " TEXT, "
                + CardsColumns.CARD_CARD + " TEXT, "
                + CardsColumns.CARD_PHONE + " TEXT, "
                + CardsColumns.CARD_HOUR + " TEXT, "
                + CardsColumns.CARD_AMPM + " TEXT, "
                + CardsColumns.CARD_LAST_BALANCE + " TEXT DEFAULT NULL)");

        upgradeAtoB(db);
    }

    private void upgradeAtoB(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + Tables.AGENCIES + " ("
                + AgenciesColumns.AGENCY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + AgenciesColumns.AGENCY_ADDRESS + " TEXT, "
                + AgenciesColumns.AGENCY_NAME + " TEXT, "
                + AgenciesColumns.AGENCY_NEIGHBORHOOD + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.e(TAG, "onUpgrade() from " + oldVersion + " to " + newVersion);

        // Current DB version. We update this variable as we perform upgrades to reflect
        // the current version we are in.
        int version = oldVersion;

        // Indicates whether the data we currently have should be invalidated as a
        // result of the db upgrade. Default is true (invalidate); if we detect that this
        // is a trivial DB upgrade, we set this to false.
        boolean dataInvalidated = true;

        // Check if we can upgrade from release A to release B
        if (version == VER_2013_RELEASE_A) {
            Log.e(TAG, "Upgrading database from 2013 release A to 2014 release B.");
            upgradeAtoB(db);
            version = VER_2014_RELEASE_B;
        }

        // at this point, we ran out of upgrade logic, so if we are still at the wrong
        // version, we have no choice but to delete everything and create everything again.
        if (version != CUR_DATABASE_VERSION) {
            db.execSQL("DROP TABLE IF EXISTS " + Tables.CARDS);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.AGENCIES);

            onCreate(db);
            version = CUR_DATABASE_VERSION;
        }
    }
}