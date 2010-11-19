package com.midlandroid.apps.android.laptimer;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import com.midlandroid.apps.android.laptimer.util.SimpleFileAccess;
import com.midlandroid.apps.android.laptimer.util.TextUtil;
import com.midlandroid.apps.android.laptimer.util.TimerHistoryDbRecord;
import com.midlandroid.apps.android.laptimer.util.db.OpenDatabaseHelper;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class TimerHistory extends Activity {
	
	private ListView historyList;
	private List<TimerHistoryDbRecord> timerHistory;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.timer_history);
		
		historyList = (ListView) findViewById(R.id.history_list);
		historyList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
				_showHistoryDialog(timerHistory.get(pos));
			}
		});
		
		historyList.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View view, int pos, long id) {
				// TODO replace this with a pop-up window displaying the contents of the history
				Toast.makeText(getApplicationContext(), "Manage item coming soon.", Toast.LENGTH_SHORT).show();
				
				// long click was handled
				return true;
			}
		});
		

		OpenDatabaseHelper dbHelper = new OpenDatabaseHelper(this);
		timerHistory = dbHelper.selectAllTimerHistories();
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
        List<TimerHistoryDbRecord> tmpHistory = timerHistory;
        
        // Detect whether or not the application will be able to write to the storage device.
        String storageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(storageState)) {
			for (TimerHistoryDbRecord result : tmpHistory) {
				new SimpleFileAccess().showOutFileAlertPromptAndWriteTo(this,
						//"/sdcard/laptimer/"+DateFormat.format("yyyyMMdd-kkmmss",new Date().getTime())+"/laptimer_"+DateFormat.format("yyyyMMdd-kkmmss",result.getStartedAt()) + ".txt",
						"/sdcard/laptimer_"+DateFormat.format("yyyyMMdd-kkmmss",result.getStartedAt()) + ".txt",
						result.getHistory());
			}
        } else {
        	// Create the alert that will prompt the user
        	final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        	alert.setTitle("File Selection");
        	alert.setMessage("SD card is not mounted, unable to save timer history.");

        	// Create the click listeners for user response
        	alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
    			@Override
    			public void onClick(DialogInterface dialog, int which) {
    			}
    		});

        	// Display the alert
        	alert.show();
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
        List<TimerHistoryDbRecord> tmpHistory = timerHistory;
		
        // Get the values from each element in the results list
		List<String> listItems = new ArrayList<String>();
		for (TimerHistoryDbRecord result : tmpHistory) {
			listItems.add(new String(
					TextUtil.formatDateToString(result.getStartedAt()) + "\n" +
					"Duration: " + TextUtil.formatDateToString(result.getDuration(), numFormat)));
		}
		
		// Add the values to the list
		historyList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems.toArray(new String[0])));
	}
	
	
	/**
	 * Displays a popup dialog displaying the selected timer history's history
	 * @param record
	 */
	private void _showHistoryDialog(TimerHistoryDbRecord record) {
		Dialog dialog = new Dialog(this);
		dialog.setContentView(R.layout.history_dialog_layout);
		dialog.setTitle("Recorded Event History");
		
		TextView text = (TextView) dialog.findViewById(R.id.history_dialog_text);
		text.setText(record.getHistory());
		
		dialog.show();
	}
}
