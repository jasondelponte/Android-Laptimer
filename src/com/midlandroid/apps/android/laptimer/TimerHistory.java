package com.midlandroid.apps.android.laptimer;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import com.midlandroid.apps.android.laptimer.util.OpenDatabaseHelper;
import com.midlandroid.apps.android.laptimer.util.TextUtil;
import com.midlandroid.apps.android.laptimer.util.TimerHistoryDbResult;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;


public class TimerHistory extends Activity {
	
	private ListView historyList;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.timer_history);
		
		historyList = (ListView) findViewById(R.id.history_list);
		
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

		// Create the number formatter that will be used later
		NumberFormat numFormat = NumberFormat.getInstance();
        numFormat.setMinimumIntegerDigits(2);
        numFormat.setMaximumIntegerDigits(2);
        numFormat.setParseIntegerOnly(true);
		
        // Get the values from each element in the results list
		List<String> listItems = new ArrayList<String>();
		for (TimerHistoryDbResult result : results) {
			listItems.add(new String(
					TextUtil.formatDateToString(result.getStartedAt()) + "\n" +
					"Duration: " + TextUtil.formatDateToString(result.getDuration(), numFormat)));
		}
		
		// Add the values to the list
		historyList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems.toArray(new String[0])));
	}
}
