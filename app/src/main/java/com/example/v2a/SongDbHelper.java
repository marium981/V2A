package com.example.v2a;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;

public class SongDbHelper extends SQLiteOpenHelper {
    /** Name of the database file */
    private static final String DATABASE_NAME = "songs.db";

    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 1;

    public SongDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the pets table
        String SQL_CREATE_SONGS_TABLE =  "CREATE TABLE " + SongContract.SongEntry.TABLE_NAME + " ("
                + SongContract.SongEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + SongContract.SongEntry.COLUMN_SONG_NAME + " TEXT, "
                + SongContract.SongEntry.COLUMN_SONG_LINK + " TEXT NOT NULL);";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_SONGS_TABLE);

        String SQL_CREATE_DOWNLOAD_TABLE =  "CREATE TABLE " + SongContract.SongEntry.DOWNLOAD_TABLE_NAME + " ("
                + SongContract.SongEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + SongContract.SongEntry.DOWNLOAD_COLUMN_SONG_NAME + " TEXT NOT NULL, "
                + SongContract.SongEntry.DOWNLOAD_COLUMN_SONG_LINK + " TEXT NOT NULL);";

        db.execSQL(SQL_CREATE_DOWNLOAD_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public Cursor raw() {

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM " + SongContract.SongEntry.TABLE_NAME, new String[]{});

        return res;
    }

    public ArrayList<HashMap<String, String>> getAllProducts() {
        ArrayList<HashMap<String, String>> downloadList;
        downloadList = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT  * FROM " + SongContract.SongEntry.DOWNLOAD_TABLE_NAME;
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                //Id, Company,Name,Price
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("Id", cursor.getString(0));
                map.put("name", cursor.getString(1));
                map.put("link", cursor.getString(2));
                downloadList.add(map);
            } while (cursor.moveToNext());
        }

        return downloadList;
    }

}
