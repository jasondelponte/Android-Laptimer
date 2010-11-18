package com.midlandroid.apps.android.laptimer.util.db;

import java.util.ArrayList;
import java.util.List;

import com.midlandroid.apps.android.laptimer.util.TimerHistoryDbResult;

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
    
    
    /**
     * Closes the connection to the database.
     */
    public void close() {
    	db.close();
    }
    
    
    public List<TimerHistoryDbResult> selectAll() {
    	List<TimerHistoryDbResult> results = new ArrayList<TimerHistoryDbResult>();
    	Cursor cursor = db.query(TIMER_HISTORY_TABLE_NAME,
    			new String[] {
    			    TIMER_HISTORY_COL_ID,
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
    					cursor.getInt(TIMER_HISTORY_COL_ID_IDX),
    					cursor.getLong(TIMER_HISTORY_COL_STARTED_AT_IDX),
    					cursor.getLong(TIMER_HISTORY_COL_FINISHED_AT_IDX),
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
