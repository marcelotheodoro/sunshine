/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.mtheodor.sunshine.app.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import com.example.mtheodor.sunshine.app.data.WeatherContract.LocationEntry;
import com.example.mtheodor.sunshine.app.data.WeatherContract.WeatherEntry;

import java.util.HashSet;

public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    // Since we want each test to start with a clean slate
    void deleteTheDatabase() {
        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
    }

    /*
        This function gets called before each test is executed to delete the database.  This makes
        sure that we always have a clean test.
     */
    public void setUp() {
        deleteTheDatabase();
    }

    /*
        Students: Uncomment this test once you've written the code to create the Location
        table.  Note that you will have to have chosen the same column names that I did in
        my solution for this test to compile, so if you haven't yet done that, this is
        a good time to change your column names to match mine.

        Note that this only tests that the Location table has the correct columns, since we
        give you the code for the weather table.  This test does not look at the
     */
    public void testCreateDb() throws Throwable {
        // build a HashSet of all of the table names we wish to look for
        // Note that there will be another table in the DB that stores the
        // Android metadata (db version information)
        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(LocationEntry.TABLE_NAME);
        tableNameHashSet.add(WeatherEntry.TABLE_NAME);

        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new WeatherDbHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        // have we created the tables we want?
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertTrue("Error: This means that the database has not been created correctly",
                c.moveToFirst());

        // verify that the tables have been created
        do {
            tableNameHashSet.remove(c.getString(0));
        } while( c.moveToNext() );

        // if this fails, it means that your database doesn't contain both the location entry
        // and weather entry tables
        assertTrue("Error: Your database was created without both the location entry and weather entry tables",
                tableNameHashSet.isEmpty());

        // now, do our tables contain the correct columns?
        c = db.rawQuery("PRAGMA table_info(" + LocationEntry.TABLE_NAME + ")",
                null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> locationColumnHashSet = new HashSet<String>();
        locationColumnHashSet.add(LocationEntry._ID);
        locationColumnHashSet.add(LocationEntry.COLUMN_CITY_NAME);
        locationColumnHashSet.add(LocationEntry.COLUMN_COORD_LAT);
        locationColumnHashSet.add(LocationEntry.COLUMN_COORD_LONG);
        locationColumnHashSet.add(LocationEntry.COLUMN_LOCATION_SETTING);

        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            locationColumnHashSet.remove(columnName);
        } while(c.moveToNext());

        // if this fails, it means that your database doesn't contain all of the required location
        // entry columns
        assertTrue("Error: The database doesn't contain all of the required location entry columns",
                locationColumnHashSet.isEmpty());
        db.close();
    }

    /*
        Students:  Here is where you will build code to test that we can insert and query the
        location database.  We've done a lot of work for you.  You'll want to look in TestUtilities
        where you can uncomment out the "createNorthPoleLocationValues" function.  You can
        also make use of the ValidateCurrentRecord function from within TestUtilities.
    */
    public void testLocationTable() {
        // First step: Get reference to writable database
        WeatherDbHelper dbHelper = new WeatherDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Create ContentValues of what you want to insert
        // (you can use the createNorthPoleLocationValues if you wish)
        long locationRowId = createLocation(db);

        // Query the database and receive a Cursor back
        Cursor foundLocations = db.query(LocationEntry.TABLE_NAME,
                null,
                LocationEntry._ID + " = ?",
                new String[]{Long.toString(locationRowId)},
                null, null, null);

        // Move the cursor to a valid database row
        assertEquals(foundLocations.getCount(), 1);
        foundLocations.moveToFirst();

        // Validate data in resulting Cursor with the original ContentValues
        // (you can use the validateCurrentRecord function in TestUtilities to validate the
        // query if you like)
        assertEquals(foundLocations.getString(foundLocations.getColumnIndex(LocationEntry.COLUMN_LOCATION_SETTING)), TestUtilities.TEST_LOCATION);
        assertEquals(foundLocations.getString(foundLocations.getColumnIndex(LocationEntry.COLUMN_CITY_NAME)), TestUtilities.NORTH_POLE);
        assertEquals(foundLocations.getDouble(foundLocations.getColumnIndex(LocationEntry.COLUMN_COORD_LAT)), TestUtilities.COORD_LAT);
        assertEquals(foundLocations.getDouble(foundLocations.getColumnIndex(LocationEntry.COLUMN_COORD_LONG)), TestUtilities.COORD_LONG);

        // Finally, close the cursor and database
        foundLocations.close();
        db.close();
    }

    private long createLocation(SQLiteDatabase db) {
        ContentValues testValues = TestUtilities.createNorthPoleLocationValues();
        return insert(db, testValues, LocationEntry.TABLE_NAME);
    }

    private long insert(SQLiteDatabase db, ContentValues testValues, String tableName) {
        long id = db.insert(tableName, null, testValues);
        assertTrue("Error: Failure to insert " + tableName, id != -1);
        return id;
    }

    /*
        Students:  Here is where you will build code to test that we can insert and query the
        database.  We've done a lot of work for you.  You'll want to look in TestUtilities
        where you can use the "createWeatherValues" function.  You can
        also make use of the validateCurrentRecord function from within TestUtilities.
     */
    public void testWeatherTable() {
        WeatherDbHelper dbHelper = new WeatherDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        long locationId = createLocation(db);
        ContentValues weatherValues = TestUtilities.createWeatherValues(locationId);
        long weatherId = insert(db, weatherValues, WeatherEntry.TABLE_NAME);

        Cursor foundWeathers = db.query(WeatherEntry.TABLE_NAME,
                null,
                WeatherEntry._ID + " = ?",
                new String[]{Long.toString(weatherId)},
                null, null, null);

        assertEquals(foundWeathers.getCount(), 1);
        foundWeathers.moveToFirst();

        assertEquals(foundWeathers.getLong(foundWeathers.getColumnIndex(WeatherEntry.COLUMN_LOC_KEY)), locationId);
        assertEquals(foundWeathers.getLong(foundWeathers.getColumnIndex(WeatherEntry.COLUMN_DATE)), TestUtilities.TEST_DATE);
        assertEquals(foundWeathers.getDouble(foundWeathers.getColumnIndex(WeatherEntry.COLUMN_DEGREES)), TestUtilities.DEGREES);
        assertEquals(foundWeathers.getDouble(foundWeathers.getColumnIndex(WeatherEntry.COLUMN_HUMIDITY)), TestUtilities.HUMIDITY);
        assertEquals(foundWeathers.getDouble(foundWeathers.getColumnIndex(WeatherEntry.COLUMN_PRESSURE)), TestUtilities.PRESSURE);
        assertEquals(foundWeathers.getInt(foundWeathers.getColumnIndex(WeatherEntry.COLUMN_MAX_TEMP)), TestUtilities.MAX_TEMP);
        assertEquals(foundWeathers.getInt(foundWeathers.getColumnIndex(WeatherEntry.COLUMN_MIN_TEMP)), TestUtilities.MIN_TEMP);
        assertEquals(foundWeathers.getString(foundWeathers.getColumnIndex(WeatherEntry.COLUMN_SHORT_DESC)), TestUtilities.SHORT_DESC);
        assertEquals(foundWeathers.getDouble(foundWeathers.getColumnIndex(WeatherEntry.COLUMN_WIND_SPEED)), TestUtilities.WIND_SPEED);
        assertEquals(foundWeathers.getInt(foundWeathers.getColumnIndex(WeatherEntry.COLUMN_WEATHER_ID)), TestUtilities.WEATHER_ID);

        foundWeathers.close();
        db.close();
    }


    /*
        Students: This is a helper method for the testWeatherTable quiz. You can move your
        code from testLocationTable to here so that you can call this code from both
        testWeatherTable and testLocationTable.
     */
    public long insertLocation() {
        return -1L;
    }
}
