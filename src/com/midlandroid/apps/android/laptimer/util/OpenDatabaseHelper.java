package com.midlandroid.apps.android.laptimer.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

public class OpenDatabaseHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "laptimer.db";
    
    // Timer History Table
    private static final String TIMER_HISTORY_COL_STARTED_AT   = "started_at";
    private static final int TIMER_HISTORY_COL_STARTED_AT_IDX  = 0;
    private static final String TIMER_HISTORY_COL_FINISHED_AT  = "finished_at";
    private static final int TIMER_HISTORY_COL_FINISHED_AT_IDX = 1;
    private static final String TIMER_HISTORY_COL_DURATION     = "duration";
    private static final int TIMER_HISTORY_COL_DURATION_IDX    = 2;
    private static final String TIMER_HISTORY_COL_HISTORY      = "history";
    private static final int TIMER_HISTORY_COL_HISTORY_IDX     = 3;
    
    private static final String TIMER_HISTORY_TABLE_NAME   = "timer_history";
    private static final String TIMER_HISTORY_TABLE_CREATE =
            "CREATE TABLE " + TIMER_HISTORY_TABLE_NAME + 
	            " (" +
		            TIMER_HISTORY_COL_STARTED_AT  + " INTEGER, " + 
		            TIMER_HISTORY_COL_FINISHED_AT + " INTEGER, " +
		            TIMER_HISTORY_COL_DURATION    + " INTEGER, " +
		            TIMER_HISTORY_COL_HISTORY     + " TEXT"      +
	            ");";
    private static final String TIMER_HISTORY_TABLE_DROP = 
		"DROP TABLE IF EXISTS "+TIMER_HISTORY_TABLE_NAME+";";
    
    private SQLiteStatement insertStmt;
    private static final String TIMER_HISTORY_INSERT = 
    	"insert into " + TIMER_HISTORY_TABLE_NAME +
	    	"(" +
	    		TIMER_HISTORY_COL_STARTED_AT  +"," +
	    		TIMER_HISTORY_COL_FINISHED_AT +"," +
	    		TIMER_HISTORY_COL_DURATION    +"," +
	    		TIMER_HISTORY_COL_HISTORY     +
			") values (?,?,?,?)";
    
    
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
		db = openHelper.getWritableDatabase();
		
		// precompile SQL statements
		insertStmt = db.compileStatement(TIMER_HISTORY_INSERT);
	}
    
    
    /**
     * Inserts the values defining a timer history record into the database.
     * @param startedAt
     * @param finishedAt
     * @param duration
     * @param history
     * @return
     */
    public long insert(final long startedAt, final long finishedAt,
    		final long duration, final String history) {
    	// the bind uses a 1 based index not 0
    	insertStmt.bindLong(TIMER_HISTORY_COL_STARTED_AT_IDX+1, startedAt);
    	insertStmt.bindLong(TIMER_HISTORY_COL_FINISHED_AT_IDX+1, finishedAt);
    	insertStmt.bindLong(TIMER_HISTORY_COL_DURATION_IDX+1, duration);
    	insertStmt.bindString(TIMER_HISTORY_COL_HISTORY_IDX+1, history);
    	
        return insertStmt.executeInsert();
    }
    
    
    public void update() {
    	
    }
    
    
    public List<TimerHistoryDbResult> selectAll() {
    	List<TimerHistoryDbResult> results = new ArrayList<TimerHistoryDbResult>();
    	//Cursor cursor = db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
    	Cursor cursor = db.query(TIMER_HISTORY_TABLE_NAME,
    			new String[] {
		            TIMER_HISTORY_COL_STARTED_AT,
		            TIMER_HISTORY_COL_FINISHED_AT,
		            TIMER_HISTORY_COL_DURATION,
		            TIMER_HISTORY_COL_HISTORY
	            },
    			null, null, null, null,
    			TIMER_HISTORY_COL_STARTED_AT+" desc");
    	
    	if (cursor.moveToFirst()) {
    		do {
    			// Get the value from the database and add it to the
    			// result list.
    			TimerHistoryDbResult result = new TimerHistoryDbResult(
    					new Date(cursor.getLong(TIMER_HISTORY_COL_STARTED_AT_IDX)),
    					new Date(cursor.getLong(TIMER_HISTORY_COL_FINISHED_AT_IDX)),
    					cursor.getLong(TIMER_HISTORY_COL_DURATION_IDX),
    					cursor.getString(TIMER_HISTORY_COL_HISTORY_IDX));
    			results.add(result);
    			
    		} while (cursor.moveToNext());
    	}
    	
    	// Release and close the db cursor
    	if (cursor != null && !cursor.isClosed()) {
    		cursor.close();
        }
    	
    	return results;
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
