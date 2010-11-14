package com.midlandroid.apps.android.laptimer.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class OpenDatabaseHelper {
    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "laptimer.db";
    
    // Timer History Table
    private static final String TIMER_HISTORY_COL_STARTED_AT = "started_at";
    private static final String TIMER_HISTORY_COL_DURATION   = "duration";
    private static final String TIMER_HISTORY_COL_LAP_COUNT  = "lap_count";
    private static final String TIMER_HISTORY_COL_HISTORY    = "history";
    private static final String TIMER_HISTORY_TABLE_NAME = "timer_history";
    private static final String TIMER_HISTORY_TABLE_CREATE =
            "CREATE TABLE " + TIMER_HISTORY_TABLE_NAME + " (" +
	            TIMER_HISTORY_COL_STARTED_AT + " INTEGER, " + 
	            TIMER_HISTORY_COL_DURATION   + " INTEGER, " + 
	            TIMER_HISTORY_COL_LAP_COUNT  + " INTEGER, " + 
	            TIMER_HISTORY_COL_HISTORY + " TEXT);";
    private static final String TIMER_HISTORY_TABLE_DROP = 
		"DROP TABLE IF EXISTS " + TIMER_HISTORY_TABLE_NAME + ";";
    
    
    // instance variables
    private Context context;
    private SQLiteDatabase db;
    
    
    /**
     * Create a new instance of this database helper
     * @param context
     */
    public OpenDatabaseHelper(Context context) {
		this.context = context;
		OpenHelper openHelper = new OpenHelper(this.context);
		this.db = openHelper.getWritableDatabase();
	}
    
    public void insert(final long startedAt, final long duration, final int lapCount, final String history) {
    	
    }
    
    public void update() {
    	
    }
    
    public void selectAll() {
    	
    }
    
	
	private static class OpenHelper extends SQLiteOpenHelper {
		OpenHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}
	
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(TIMER_HISTORY_TABLE_CREATE);
		}
	
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w("Example", "Upgrading database, this will drop tables and recreate.");
			
			db.execSQL(TIMER_HISTORY_TABLE_DROP);
			onCreate(db);
		}
	}
}
