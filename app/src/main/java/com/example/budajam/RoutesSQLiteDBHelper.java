package com.example.budajam;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class RoutesSQLiteDBHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Routes";
    public static final String ROUTES_TABLE_NAME = "routes";
    public static final String ROUTES_COLUMN_PLACE = "place";
    public static final String ROUTES_COLUMN_NAME = "name";
    public static final String ROUTES_COLUMN_DIFFICULTY = "difficulty";
    public static final String ROUTES_COLUMN_LENGTH = "length";
    public static final String ROUTES_COLUMN_DIFFCHANGER = "diffchanger";
    public static final String ROUTES_COLUMN_KEY = "_key";

    public RoutesSQLiteDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE " + ROUTES_TABLE_NAME + " (" +
                ROUTES_COLUMN_KEY + " INTEGER PRIMARY KEY, " +
                ROUTES_COLUMN_PLACE + " TEXT, " +
                ROUTES_COLUMN_NAME + " TEXT, " +
                ROUTES_COLUMN_DIFFICULTY + " LONG, " +
                ROUTES_COLUMN_LENGTH + " LONG, " +
                ROUTES_COLUMN_DIFFCHANGER + " TEXT" + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ROUTES_TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
