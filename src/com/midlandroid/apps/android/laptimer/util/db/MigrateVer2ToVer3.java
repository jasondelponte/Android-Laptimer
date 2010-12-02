package com.midlandroid.apps.android.laptimer.util.db;

import java.util.ArrayList;
import java.util.List;

import com.midlandroid.apps.android.laptimer.util.TimerHistoryDbRecord;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;


/**
 * Database version 3 adds the description column to the timer history table.
 * @author jason
 *
 */
public class MigrateVer2ToVer3 extends Migration {
	private static final String LOG_TAG = MigrateVer1ToVer2.class.getSimpleName();
	
    // Timer History Table
    private static final int   OLD_TIMER_HISTORY_COL_ID_IDX          = 0;
    private static final int   OLD_TIMER_HISTORY_COL_STARTED_AT_IDX  = 1;
    private static final int   OLD_TIMER_HISTORY_COL_FINISHED_AT_IDX = 2;
    private static final int   OLD_TIMER_HISTORY_COL_DURATION_IDX    = 3;
    private static final int   OLD_TIMER_HISTORY_COL_HISTORY_IDX     = 4;
    
//    // new timer history table column indexes
//    private static final int   NEW_TIMER_HISTORY_COL_ID_IDX          = 0;
//    private static final int   NEW_TIMER_HISTORY_COL_STARTED_AT_IDX  = 1;
//    private static final int   NEW_TIMER_HISTORY_COL_FINISHED_AT_IDX = 2;
//    private static final int   NEW_TIMER_HISTORY_COL_DURATION_IDX    = 3;
//    private static final int   NEW_TIMER_HISTORY_COL_HISTORY_IDX     = 4;


    private static final String TIMER_HISTORY_COL_ID           = "id";
    private static final String TIMER_HISTORY_COL_STARTED_AT   = "started_at";
    private static final String TIMER_HISTORY_COL_DESC         = "desc";
    private static final String TIMER_HISTORY_COL_FINISHED_AT  = "finished_at";
    private static final String TIMER_HISTORY_COL_DURATION     = "duration";
    private static final String TIMER_HISTORY_COL_HISTORY      = "history";
    
    private static final String TIMER_HISTORY_TABLE_NAME   = "timer_history";
    private static final String TIMER_HISTORY_TABLE_CREATE =
        "CREATE TABLE " + TIMER_HISTORY_TABLE_NAME + 
        " (" +
            TIMER_HISTORY_COL_ID          + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            TIMER_HISTORY_COL_STARTED_AT  + " INTEGER, " +
            TIMER_HISTORY_COL_DESC        + " STRING, "  +
            TIMER_HISTORY_COL_FINISHED_AT + " INTEGER, " +
            TIMER_HISTORY_COL_DURATION    + " INTEGER, " +
            TIMER_HISTORY_COL_HISTORY     + " TEXT"      +
        ");";
    
    private static final String TIMER_HISTORY_TABLE_DROP = 
		"DROP TABLE IF EXISTS "+TIMER_HISTORY_TABLE_NAME+";";

    private SQLiteStatement insertStmt;
    private static final String TIMER_HISTORY_INSERT = 
    	"INSERT INTO " + TIMER_HISTORY_TABLE_NAME +
	    	"(" +
	    		TIMER_HISTORY_COL_STARTED_AT  +"," +
	    		TIMER_HISTORY_COL_DESC        +"," +
	    		TIMER_HISTORY_COL_FINISHED_AT +"," +
	    		TIMER_HISTORY_COL_DURATION    +"," +
	    		TIMER_HISTORY_COL_HISTORY     +
			") values (?,?,?,?,?)";

    
    
    public MigrateVer2ToVer3(SQLiteDatabase db)  {
    	super(db);

    	// precompile SQL statements
    	insertStmt = db.compileStatement(TIMER_HISTORY_INSERT);
    }
	
	
	public void doMigrate() {
		Log.i(LOG_TAG, "Migrating database from Version 1 to Version 2");
		
		List<TimerHistoryDbRecord> results = _selectAll();
		
		// Drop and recreate the timer history table
		db.execSQL(TIMER_HISTORY_TABLE_DROP);
		db.execSQL(TIMER_HISTORY_TABLE_CREATE);
		
		// Re-populate the table with our saved results
		for (TimerHistoryDbRecord result : results) {
			insert(result.getStartedAt(), result.getDesc(), result.getFinishedAt(),
					result.getDuration(), result.getHistory());
		}
		
	}
	
	
	
	/**
	 * Query the database timer history table for all results.
	 * @return
	 */
	private List<TimerHistoryDbRecord> _selectAll() {
		// Get the existing data
		List<TimerHistoryDbRecord> results = new ArrayList<TimerHistoryDbRecord>();
		
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
			// Get the value from the database and add it to the
			// result list.
			TimerHistoryDbRecord result = new TimerHistoryDbRecord(
					cursor.getInt(OLD_TIMER_HISTORY_COL_ID_IDX),
					cursor.getLong(OLD_TIMER_HISTORY_COL_STARTED_AT_IDX),
					"",
					cursor.getLong(OLD_TIMER_HISTORY_COL_FINISHED_AT_IDX),
					cursor.getLong(OLD_TIMER_HISTORY_COL_DURATION_IDX),
					cursor.getString(OLD_TIMER_HISTORY_COL_HISTORY_IDX));
			results.add(result);
    	}
    	
    	// Release and close the db cursor
    	if (cursor != null && !cursor.isClosed()) {
    		cursor.close();
        }
    	
    	return results;
	}
	
    /**
	* Inserts the values defining a timer history record into the database.
	* @param startedAt
	* @param finishedAt
	* @param duration
	* @param history
	* @return
	*/
	private void insert(final long startedAt, final String desc, final long finishedAt,
			final long duration, final String history) {
		 // the bind uses a 1 based index not 0
		insertStmt.bindLong   (1, startedAt);
		insertStmt.bindString (2, desc);
		insertStmt.bindLong   (2, finishedAt);
		insertStmt.bindLong   (3, duration);
		insertStmt.bindString (4, history);
		// perform the insert
		insertStmt.executeInsert();
	}
}
