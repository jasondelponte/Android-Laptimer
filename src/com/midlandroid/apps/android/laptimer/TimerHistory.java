package com.midlandroid.apps.android.laptimer;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import com.midlandroid.apps.android.laptimer.util.OpenDatabaseHelper;
import com.midlandroid.apps.android.laptimer.util.SimpleFileAccess;
import com.midlandroid.apps.android.laptimer.util.TextUtil;
import com.midlandroid.apps.android.laptimer.util.TimerHistoryDbResult;

import android.app.Activity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;


public class TimerHistory extends Activity {
	
	private ListView historyList;
	private List<TimerHistoryDbResult> timerHistory;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.timer_history);
		
		historyList = (ListView) findViewById(R.id.history_list);
		historyList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
				// TODO replace this with a pop-up window displaying the contents of the history
				Toast.makeText(getApplicationContext(), "View item coming soon.", Toast.LENGTH_LONG).show();
			}
		});
		
		historyList.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View view, int pos, long id) {
				// TODO replace this with a pop-up window displaying the contents of the history
				Toast.makeText(getApplicationContext(), "Manage item coming soon.", Toast.LENGTH_LONG).show();
				
				// long click was handled
				return true;
			}
		});
		

		OpenDatabaseHelper dbHelper = new OpenDatabaseHelper(this);
		timerHistory = dbHelper.selectAll();
    	dbHelper.close();
		
		_populateListWithTimerHistory();
	}
	
    ////////////////////////////////////
    // Options Menu
    ////////////////////////////////////
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	getMenuInflater().inflate(R.menu.history_menu, menu);
    	   	
		return true;
    }
    
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()) {
    	case R.id.mi_write_all_to_sdcard:
    		_writeAllHistoryToSdCard();
    		return true;
    	}
    	return false;
    }
	
	
    ////////////////////////////////////
    // Private Methods	
    ////////////////////////////////////
    
	private void _writeAllHistoryToSdCard() {
		//SimpleFileAccess fileAccess = new SimpleFileAccess();

		// Create the number formatter that will be used later
		NumberFormat numFormat = NumberFormat.getInstance();
        numFormat.setMinimumIntegerDigits(2);
        numFormat.setMaximumIntegerDigits(2);
        numFormat.setParseIntegerOnly(true);
        
        // Local reference to the timer history list
        List<TimerHistoryDbResult> tmpHistory = timerHistory;

		for (TimerHistoryDbResult result : tmpHistory) {
			new SimpleFileAccess().showOutFileAlertPromptAndWriteTo(this,
					//"/sdcard/laptimer/"+DateFormat.format("yyyyMMdd-kkmmss",new Date().getTime())+"/laptimer_"+DateFormat.format("yyyyMMdd-kkmmss",result.getStartedAt()) + ".txt",
					"/sdcard/laptimer_"+DateFormat.format("yyyyMMdd-kkmmss",result.getStartedAt()) + ".txt",
					"Started on: "+TextUtil.formatDateToString(result.getStartedAt())             + "\n" +
						"Duration: "+TextUtil.formatDateToString(result.getDuration(), numFormat) + "\n\n" +
						"History:\n" + result.getHistory());
		}
	}

	/**
	 * Using the values returned by querying for
	 * saved timer histories, build the history list.
	 */
	private void _populateListWithTimerHistory() {

		// Create the number formatter that will be used later
		NumberFormat numFormat = NumberFormat.getInstance();
        numFormat.setMinimumIntegerDigits(2);
        numFormat.setMaximumIntegerDigits(2);
        numFormat.setParseIntegerOnly(true);
        
        // Local reference to the timer history list
        List<TimerHistoryDbResult> tmpHistory = timerHistory;
		
        // Get the values from each element in the results list
		List<String> listItems = new ArrayList<String>();
		for (TimerHistoryDbResult result : tmpHistory) {
			listItems.add(new String(
					TextUtil.formatDateToString(result.getStartedAt()) + "\n" +
					"Duration: " + TextUtil.formatDateToString(result.getDuration(), numFormat)));
		}
		
		// Add the values to the list
		historyList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems.toArray(new String[0])));
	}
}
