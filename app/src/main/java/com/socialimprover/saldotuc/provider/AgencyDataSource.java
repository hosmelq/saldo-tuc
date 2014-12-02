package com.socialimprover.saldotuc.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.socialimprover.saldotuc.models.Agency;
import com.socialimprover.saldotuc.provider.SaldoTucContract.AgenciesColumns;
import com.socialimprover.saldotuc.provider.SaldoTucHelper.Tables;

import java.util.ArrayList;
import java.util.List;

public class AgencyDataSource {

    private SaldoTucHelper mSaldoTucHelper; // Helper class for creating and opening the DB

    public AgencyDataSource(Context context) {
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
    public List<Agency> all() {
        SQLiteDatabase database = open();
        ArrayList<Agency> agencies = new ArrayList<Agency>();
        Cursor cursor = database.rawQuery("SELECT * FROM " + Tables.AGENCIES + " ORDER BY LOWER(" + AgenciesColumns.AGENCY_NEIGHBORHOOD + ")", null);

        if (cursor.moveToFirst()) {
            do {
                Agency agency = new Agency(getIntFromColumnName(cursor, AgenciesColumns.AGENCY_ID),
                        getStringFromColumnName(cursor, AgenciesColumns.AGENCY_ADDRESS),
                        getStringFromColumnName(cursor, AgenciesColumns.AGENCY_NAME),
                        getStringFromColumnName(cursor, AgenciesColumns.AGENCY_NEIGHBORHOOD));

                agencies.add(agency);
            } while (cursor.moveToNext());
        }

        cursor.close();
        close(database);

        return agencies;
    }

    public void create(Agency agency) {
        SQLiteDatabase database = open();

        ContentValues values = new ContentValues();
        values.put(AgenciesColumns.AGENCY_ADDRESS, agency.getAddress());
        values.put(AgenciesColumns.AGENCY_NAME, agency.getName());
        values.put(AgenciesColumns.AGENCY_NEIGHBORHOOD, agency.getNeighborhood());

        database.insert(Tables.AGENCIES, null, values);

        close(database);
    }

    public void sync(List<Agency> agencies) {
        SQLiteDatabase database = open();

        database.beginTransaction();
        database.execSQL("DELETE FROM " + Tables.AGENCIES);

        for (Agency agency : agencies) {
            ContentValues values = new ContentValues();
            values.put(AgenciesColumns.AGENCY_ADDRESS, agency.getAddress());
            values.put(AgenciesColumns.AGENCY_NAME, agency.getName());
            values.put(AgenciesColumns.AGENCY_NEIGHBORHOOD, agency.getNeighborhood());

            database.insert(Tables.AGENCIES, null, values);
        }

        database.setTransactionSuccessful();
        database.endTransaction();
        close(database);
    }

}
