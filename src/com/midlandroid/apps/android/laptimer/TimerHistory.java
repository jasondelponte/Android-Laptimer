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
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;


public class TimerHistory extends Activity {
	
	private ListView historyList;
	private List<TimerHistoryDbRecord> timerHistory;
	private ArrayAdapter<String> adapter;
	private ArrayList<String> listItems;
	
	private OpenDatabaseHelper dbHelper;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.timer_history);
		
		// get a connection to the database
		dbHelper = new OpenDatabaseHelper(this);
		
		// Create the adapter that will be used to populate the history list
		listItems = new ArrayList<String>();
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems);
		
		// timer history list
		historyList = (ListView) findViewById(R.id.history_list);
		historyList.setAdapter(adapter);
		
		historyList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
				_showHistoryDialog(timerHistory.get(pos));
			}
		});
		historyList.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
			@Override
			public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
				MenuInflater inflater = getMenuInflater();
				inflater.inflate(R.menu.history_item_menu, menu);
			}
		});
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		_refreshHistoryList();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		
    	dbHelper.close();
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
    
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        int index = info.position;

    	switch(item.getItemId()) {
    	case R.id.write_history_to_sdcard:
    		_writeHistoryItemToSdCard(timerHistory.get(index));
    		return true;
    	case R.id.delete_saved_history:
    		dbHelper.deleteTimerHistoryById(timerHistory.get(index).getId());
    		_refreshHistoryList();
    		return true;
    	}
    	
    	return false;
    }
	
	
    ////////////////////////////////////
    // Private Methods	
    ////////////////////////////////////
    private void _writeHistoryItemToSdCard(TimerHistoryDbRecord record) {
		new SimpleFileAccess().showOutFileAlertPromptAndWriteTo(this,
				"/sdcard/laptimer_"+DateFormat.format("yyyyMMdd-kkmmss",record.getStartedAt()) + ".txt",
				record.getHistory());
    }
    
	private void _writeAllHistoryToSdCard() {
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
			for (TimerHistoryDbRecord record : tmpHistory) {
				_writeHistoryItemToSdCard(record);
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
        
        // clear the existing contents of the list
        listItems.clear();
		
        // Get the values from each element in the results list
		for (TimerHistoryDbRecord result : tmpHistory) {
			String str = new String(
					TextUtil.formatDateToString(result.getStartedAt()) + "\n" +
					"Duration: " + TextUtil.formatDateToString(result.getDuration(), numFormat));
			listItems.add(str);
		}
		adapter.notifyDataSetChanged();
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

	
	/**
	 * Clears the current timer history list and refreshes it with the
	 * data from the database
	 */
	private void _refreshHistoryList() {
		timerHistory = dbHelper.selectAllTimerHistories();
		_populateListWithTimerHistory();
	}
}
