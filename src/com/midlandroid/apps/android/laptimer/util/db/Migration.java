package com.midlandroid.apps.android.laptimer.util.db;

import android.database.sqlite.SQLiteDatabase;

public abstract class Migration {
	public static final String LOG_TAG = Migration.class.getSimpleName();

    protected SQLiteDatabase db;
    
    /**
     * Constructor for Migration objects
     * @param db open database reference
     */
    public Migration(SQLiteDatabase db) {
    	this.db = db;
    }
    
    
	public abstract void doMigrate();
}
