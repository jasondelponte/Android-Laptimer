package com.midlandroid.apps.android.laptimer;
import java.util.List;

import com.midlandroid.apps.android.laptimer.util.OpenDatabaseHelper;
import com.midlandroid.apps.android.laptimer.util.TimerHistoryDbResult;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


public class TimerHistory extends Activity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.timer_history);
		
		_populateListWithTimerHistory();
	}
	
    ////////////////////////////////////
    // Options Menu
    ////////////////////////////////////
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	getMenuInflater().inflate(R.menu.history_menu, menu);
    	
    	// Get the references to the items that will be mode
    	// based on the app's run state.
//    	saveHistoryMI = menu.findItem(R.id.mi_save_timer_history);
//    	
//    	_setMenuItemEnabledBasedOnRunState();
    	
		return true;
    }
    
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()) {
//    	case R.id.mi_reset_timer:
//    		_resetTimer();
//    		return true;
//    	case R.id.mi_save_timer_history:
//    		_saveTimerHistory();
//    		return true;
//    	case R.id.mi_timer_history:
//    		_showTimerHistory();
//    		return true;
//    	case R.id.mi_preferences:
//    		_showPreferences();
//    		return true;
    	}
    	return false;
    }
	
	
    ////////////////////////////////////
    // Private Methods	
    ////////////////////////////////////
    
	private void _populateListWithTimerHistory() {
		OpenDatabaseHelper dbHelper = new OpenDatabaseHelper(this);
		List<TimerHistoryDbResult> results = dbHelper.selectAll();
		
		for (TimerHistoryDbResult result : results) {
			
		}
	}
}
