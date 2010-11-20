package com.midlandroid.apps.android.laptimer.util.db;

import java.util.ArrayList;
import java.util.List;

import com.midlandroid.apps.android.laptimer.util.TimerHistoryDbRecord;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

public class OpenDatabaseHelper {
	private static final String LOG_TAG = OpenDatabaseHelper.class.getSimpleName();
	
    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "laptimer.db";
    
    // Timer History Table
    private static final int   TIMER_HISTORY_COL_ID_IDX          = 0;
    private static final int   TIMER_HISTORY_COL_STARTED_AT_IDX  = 1;
    private static final int   TIMER_HISTORY_COL_FINISHED_AT_IDX = 2;
    private static final int   TIMER_HISTORY_COL_DURATION_IDX    = 3;
    private static final int   TIMER_HISTORY_COL_HISTORY_IDX     = 4;

    private static final String TIMER_HISTORY_COL_ID           = "id";
    private static final String TIMER_HISTORY_COL_STARTED_AT   = "started_at";
    private static final String TIMER_HISTORY_COL_FINISHED_AT  = "finished_at";
    private static final String TIMER_HISTORY_COL_DURATION     = "duration";
    private static final String TIMER_HISTORY_COL_HISTORY      = "history";
    
    private static final String TIMER_HISTORY_TABLE_NAME   = "timer_history";
    private static final String TIMER_HISTORY_TABLE_CREATE =
            "CREATE TABLE " + TIMER_HISTORY_TABLE_NAME + 
	            " (" +
	                TIMER_HISTORY_COL_ID          + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
		            TIMER_HISTORY_COL_STARTED_AT  + " INTEGER, " + 
		            TIMER_HISTORY_COL_FINISHED_AT + " INTEGER, " +
		            TIMER_HISTORY_COL_DURATION    + " INTEGER, " +
		            TIMER_HISTORY_COL_HISTORY     + " TEXT"      +
	            ");";
    
    private SQLiteStatement insertStmt;
    private static final String TIMER_HISTORY_INSERT = 
    	"INSERT INTO " + TIMER_HISTORY_TABLE_NAME +
	    	"(" +
	    		TIMER_HISTORY_COL_STARTED_AT  +"," +
	    		TIMER_HISTORY_COL_FINISHED_AT +"," +
	    		TIMER_HISTORY_COL_DURATION    +"," +
	    		TIMER_HISTORY_COL_HISTORY     +
			") values (?,?,?,?)";
    
    private SQLiteStatement updateStmt;
    private static final String TIMER_HISTORY_UPDATE = 
    	"UPDATE " + TIMER_HISTORY_TABLE_NAME +
    		" SET " +
	    		TIMER_HISTORY_COL_STARTED_AT  +" = ?, "+
	    		TIMER_HISTORY_COL_FINISHED_AT +" = ?, "+
	    		TIMER_HISTORY_COL_DURATION    +" = ?, "+
	    		TIMER_HISTORY_COL_HISTORY     +" = ?" +
    		" WHERE "+TIMER_HISTORY_COL_STARTED_AT+" = ?;";
    
    private SQLiteStatement deleteStmt;
    private static final String TIMER_HISTORY_DELETE = 
    	"DELETE FROM " + TIMER_HISTORY_TABLE_NAME +
    		" WHERE id = ?;";
    
    private static final String TIMER_HISTORY_SELECT_BY_START_AT = 
    	TIMER_HISTORY_COL_STARTED_AT+" = ?";
    
    
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
		updateStmt = db.compileStatement(TIMER_HISTORY_UPDATE);
		deleteStmt = db.compileStatement(TIMER_HISTORY_DELETE);
	}
    
    
    /**
     * Inserts the values defining a timer history record into the database.
     * @param startedAt
     * @param finishedAt
     * @param duration
     * @param history
     * @return
     */
    public long insertTimerHistory(final long startedAt, final long finishedAt,
    		final long duration, final String history) {
    	insertStmt.bindLong   (1, startedAt);
    	insertStmt.bindLong   (2, finishedAt);
    	insertStmt.bindLong   (3, duration);
    	insertStmt.bindString (4, history);
    	
        return insertStmt.executeInsert();
    }
    
    
    /**
     * Updates a pre-existing timer history record with new information
     * @param record
     */
    public void updateTimerHistory(TimerHistoryDbRecord record) {
    	updateStmt.bindLong   (1, record.getStartedAt());
    	updateStmt.bindLong   (2, record.getFinishedAt());
    	updateStmt.bindLong   (3, record.getDuration());
    	updateStmt.bindString (4, record.getHistory());
    	updateStmt.bindLong   (5, record.getStartedAt());
    	updateStmt.execute();
    }
    
    
    /**
     * Deletes a specific record from the timer history table
     * @param id
     */
    public void deleteTimerHistoryById(final int id) {
    	deleteStmt.bindLong(1, id);
    	deleteStmt.execute();
    }
    
    
    /**
     * Selects a specific timer history from the the database
     * @param startAt
     * @return
     */
    public TimerHistoryDbRecord selectTimerHistoryByStartAt(final long startAt) {
    	TimerHistoryDbRecord record = null;
    	
    	Cursor cursor = db.query(TIMER_HISTORY_TABLE_NAME,
    			new String[] {
    			    TIMER_HISTORY_COL_ID,
		            TIMER_HISTORY_COL_STARTED_AT,
		            TIMER_HISTORY_COL_FINISHED_AT,
		            TIMER_HISTORY_COL_DURATION,
		            TIMER_HISTORY_COL_HISTORY
	            },
	            TIMER_HISTORY_SELECT_BY_START_AT, new String[] {Long.toString(startAt)},
    			null, null,
    			TIMER_HISTORY_COL_STARTED_AT+" desc");
    	
    	if (cursor.moveToFirst()) {
			// Get the first value from the database and get its values
			record = new TimerHistoryDbRecord(
					cursor.getInt(TIMER_HISTORY_COL_ID_IDX),
					cursor.getLong(TIMER_HISTORY_COL_STARTED_AT_IDX),
					cursor.getLong(TIMER_HISTORY_COL_FINISHED_AT_IDX),
					cursor.getLong(TIMER_HISTORY_COL_DURATION_IDX),
					cursor.getString(TIMER_HISTORY_COL_HISTORY_IDX));
    	}
    	
    	// Release and close the db cursor
    	if (cursor != null && !cursor.isClosed()) {
    		cursor.close();
        }
    	
    	return record;
    }
    
    /**
     * Queries and returns from the database for all timer history records
     * @return
     */
    public List<TimerHistoryDbRecord> selectAllTimerHistories() {
    	List<TimerHistoryDbRecord> record = new ArrayList<TimerHistoryDbRecord>();
    	Cursor cursor = db.query(TIMER_HISTORY_TABLE_NAME,
    			new String[] {
    			    TIMER_HISTORY_COL_ID,
		            TIMER_HISTORY_COL_STARTED_AT,
		            TIMER_HISTORY_COL_FINISHED_AT,
		            TIMER_HISTORY_COL_DURATION,
		            TIMER_HISTORY_COL_HISTORY
	            },
	            null, null,
	            null, null,
    			TIMER_HISTORY_COL_STARTED_AT+" desc");
    	
    	if (cursor.moveToFirst()) {
    		do {
    			// Get the value from the database and add it to the
    			// result list.
    			record.add(new TimerHistoryDbRecord(
    					cursor.getInt(TIMER_HISTORY_COL_ID_IDX),
    					cursor.getLong(TIMER_HISTORY_COL_STARTED_AT_IDX),
    					cursor.getLong(TIMER_HISTORY_COL_FINISHED_AT_IDX),
    					cursor.getLong(TIMER_HISTORY_COL_DURATION_IDX),
    					cursor.getString(TIMER_HISTORY_COL_HISTORY_IDX)));
    			
    		} while (cursor.moveToNext());
    	}
    	
    	// Release and close the db cursor
    	if (cursor != null && !cursor.isClosed()) {
    		cursor.close();
        }
    	
    	return record;
    }
    
    
    /**
     * Closes the connection to the database.
     */
    public void close() {
    	db.close();
    }
    
	
	private static class OpenHelper extends SQLiteOpenHelper {
		private static final String LOG_TAG = OpenHelper.class.getSimpleName();
		
		OpenHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}
	
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(TIMER_HISTORY_TABLE_CREATE);
		}
	
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(LOG_TAG, "Database table mismatch.  "+
					"Upgrading from version "+Integer.toString(oldVersion)+
					" to version + "+Integer.toString(newVersion));
			
			// Get the list of migrations to be used
			List<Migration> migrations = buildMigrationList(db, oldVersion, newVersion);
			
			// Loop over each migration updating the database
			for (Migration migration : migrations) {
				migration.doMigrate();
			}
		}
		
		/**
		 * Build the list of migrations that will be used to update the database to the target
		 * version
		 * @param db
		 * @param oldVer
		 * @param newVer
		 * @return
		 */
		private List<Migration> buildMigrationList(SQLiteDatabase db, int oldVer, int newVer) {
			List<Migration> migrations = new ArrayList<Migration>();
			
			switch (oldVer) {
			case 1:
				migrations.add(new MigrateVer1ToVer2(db));
				break;
			}
			
			return migrations;
		}
	}
}
