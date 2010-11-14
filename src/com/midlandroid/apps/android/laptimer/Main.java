package com.midlandroid.apps.android.laptimer;

import java.text.NumberFormat;

import com.midlandroid.apps.android.laptimer.background.BackgroundSrvc;
import com.midlandroid.apps.android.laptimer.background.timers.TimerUpdateUIListener;
import com.midlandroid.apps.android.laptimer.background.timers.TimerMode.RunningState;
import com.midlandroid.apps.android.laptimer.util.ServiceCommand;
import com.midlandroid.apps.android.laptimer.util.TextUtil;
import com.midlandroid.apps.android.laptimer.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class Main extends Activity implements TimerUpdateUIListener {
	private static final String LOG_TAG = Main.class.getSimpleName();
	
	// Views
	private Button startStopBtn;
	private Button lapNumBtn;
	private TextView lapTimeTxt;
	private TextView currTimeTxt;
	private TextView timerHistoryTxt;
	//private MenuItem timerModeMI;
	private MenuItem saveHistoryMI;
	
	// members
	private long currTime;
	private long lapTime;
	private int lapCount;
	
	private NumberFormat numFormat;
	private Messenger myMessenger;


    /////////////////////////////////////////////////
	// Overridden Android Activities Methods
    /////////////////////////////////////////////////
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // Create the background service
        startService(new Intent(this, BackgroundSrvc.class));
        
        // initialize the variables
        numFormat = NumberFormat.getInstance();
        numFormat.setMinimumIntegerDigits(2);
        numFormat.setMaximumIntegerDigits(2);
        numFormat.setParseIntegerOnly(true);
        
        myMessenger = new Messenger(myHandler);
    	
        currTime = lapTime = 0;
        lapCount = 1;

    	// Lap time text view
    	lapTimeTxt = (TextView)findViewById(R.id.lap_timer_txt);
    	lapTimeTxt.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (boundService.getAppPreferences().getUseOneClickTextCopy())
					_addViewTextToClipBoard(v);
			}
    	});
    	
    	// Lap increment button 
    	lapNumBtn = (Button)findViewById(R.id.lap_increment_btn);
    	lapNumBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				_lapIncrement();
			}
    	});
    	
    	// Current Time text view
    	currTimeTxt = (TextView)findViewById(R.id.timer_counter_txt);
    	currTimeTxt.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (boundService.getAppPreferences().getUseOneClickTextCopy())
					_addViewTextToClipBoard(v);
			}
    	});
    	
    	// Timer Start Stop Button
    	startStopBtn = (Button)findViewById(R.id.timer_start_stop_btn);
    	startStopBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				_startStopTimer();
			}
    	});
    	
    	// Timer History Text View
    	timerHistoryTxt = (TextView)findViewById(R.id.lap_txt);
    	timerHistoryTxt.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (boundService != null && boundService.getAppPreferences().getUseOneClickTextCopy())
					_addViewTextToClipBoard(v);
			}
    	});
    }
    
    
    @Override
    public void onResume() {
    	super.onResume();

        // Attach to the service
    	_doBindService();
    }
    
    
    @Override
    public void onPause() {
    	super.onPause();
    	
    	// Get the current running state
    	RunningState currState = RunningState.RESETTED;
    	if (boundService != null)
    		currState = boundService.getTimerState();
    	
    	// Disconnect from the service
    	_doUnbindService();
    	
    	// Shutdown the service if its not running
    	if (currState == RunningState.RESETTED)
    		stopService(new Intent(this, BackgroundSrvc.class));
    }
    
    
    /////////////////////////////////
    // Overridden controls
    /////////////////////////////////
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if (keyCode == KeyEvent.KEYCODE_MENU && event.getRepeatCount() == 0) {
    		_setMenuItemEnabledBasedOnRunState();
    	}
    	else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP && boundService != null && boundService.getAppPreferences().getUseVolumeButtonsForTimer()) {
    		_startStopTimer();
    		return true;
    	}
    	else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN && boundService != null && boundService.getAppPreferences().getUseVolumeButtonsForTimer()) {
    		_lapIncrement();
    		return true;
    	}
    	
    	return super.onKeyDown(keyCode, event);
    }
    
    
    ////////////////////////////////////
    // Options Menu
    ////////////////////////////////////
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	getMenuInflater().inflate(R.menu.timer_menu, menu);
    	
    	//timerModeMI = menu.findItem(R.id.mi_timer_mode);
    	saveHistoryMI = menu.findItem(R.id.mi_save_lap_list);
    	
    	_setMenuItemEnabledBasedOnRunState();
    	
		return true;
    }
    
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()) {
    	case R.id.mi_reset_timer:
    		_resetTimer();
    		return true;
    	case R.id.mi_save_lap_list:
    		_writeOutLapHistory();
    		return true;
    	case R.id.mi_preferences:
    		_showPreferences();
    		return true;
    	case R.id.mi_timer_mode:
    		_showTimerModeSelection();
    		return true;
    	}
    	return false;
    }
    
    /**
     * Save of the current activities state, so when it is 
     * restored this data will be returned.
     * 
     * @param state  The package container holder the activities state.
     */
    @Override
    public void onSaveInstanceState(Bundle state) {
    }
    
    
    /**
     * Restores a previously saved activity state.
     * 
     * @param state The package containing a previously saved activity state.
     */
    @Override
    public void onRestoreInstanceState(Bundle state) {
    }
    
    
    ////////////////////////////////////
    // User Action Handler Methods
    ////////////////////////////////////
    /**
     * Notifies the background service that the user has selected
     * to start or stop the timer.
     */
    private void _startStopTimer() {
    	_msgTimerService(ServiceCommand.CMD_START_STOP_TIMER);
    }
    
    
	/**
	 * Notifies the background service that the lap count should
	 * be incremented.
	 */
    private void _lapIncrement() {
    	_msgTimerService(ServiceCommand.CMD_LAP_INCREMENT);
    }
    
    
    /**
     * Resets the timer.  If the user has specified to be warned when 
     * reseting the timer prompt them instead.
     */
    private void _resetTimer() {
    	if (boundService != null && boundService.getAppPreferences().getUseSecondChanceReset()) {
    		_showPromptToResetTimer();
    	} else {
    		_doResetTimer();
    	}
    }
    
    
    /**
     * Notifies the background service that the timer should be stopped
     * and reseted.
     */
    private void _doResetTimer() {
    	_msgTimerService(ServiceCommand.CMD_RESET_TIMER);
    }
    
    
    /**
     * Notifies the background service that this UI needs to be updated.
     */
    private void _refreshUI() {
    	_msgTimerService(ServiceCommand.CMD_REFRESH_MAIN_UI);
    }
    

    ////////////////////////////////////
    // Private methods
    ////////////////////////////////////
    /**
     * Adds the text of the provided view to the android system's
     * clip board buffer.
     * @param v View with text to be added to the clip board
     */
    private void _addViewTextToClipBoard(View v) {
		 ClipboardManager clipboard = 
		      (ClipboardManager) getSystemService(CLIPBOARD_SERVICE); 

		 clipboard.setText(((TextView)v).getText());
		 Toast t = Toast.makeText(this, R.string.text_copied_to_clipboard, Toast.LENGTH_SHORT);
		 t.show();
    }
    
    
    /**
     * Using the state of the background service the menu items are
     * enabled or disabled.
     */
    private void _setMenuItemEnabledBasedOnRunState() {
    	if (boundService != null && saveHistoryMI != null /*&& timerModeMI != null*/) {
			//timerModeMI.setEnabled(boundService.getTimerState() != RunningState.RUNNING);
			saveHistoryMI.setEnabled(boundService.getTimerState() != RunningState.RUNNING);
    	}
    }
    
    
    /**
     * Creates and sends a message to the background service.
     * @param cmd Message id
     */
    private void _msgTimerService(final int cmd) {
    	_msgTimerService(cmd, null);
    }
    
    
    /**
     * Creates and sends a message to the background service with
     * the provided pay load
     * @param cmd Message id
     * @param payload Data to be send to background service
     */
    private void _msgTimerService(final int cmd, final Object payload) {
    	if (boundService != null) {
	    	Message msg = myHandler.obtainMessage();
	    	msg.what = cmd;
	    	msg.obj = payload;
	    	msg.replyTo = myMessenger;
	    	try {
				boundService.myMessenger.send(msg);
			} catch (RemoteException e) {
				Log.e(LOG_TAG, "Failed to message background service", e);
			}
    	}
    }
    
    
    /**
     * Displays the Android Preferences UI to the user.
     */
    private void _showPreferences() {
    	Intent i = new Intent(this, Preferences.class);
    	startActivity(i);
    }
    
    
    /**
     * Displays the timer mode selection UI 
     */
    private void _showTimerModeSelection() {
    	// TODO Add functionality
    }
    
    
    /**
     * Prompts the user if they are sure they really want to 
     * reset the current timer.
     */
    private void _showPromptToResetTimer() {
    	final AlertDialog.Builder alert = new AlertDialog.Builder(this);
    	
    	alert.setTitle("Confirm Timer Reset");
    	alert.setMessage("Are you sure you want to reset the timer?");
    	
    	alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				_doResetTimer();
			}
		});
    	
    	alert.setNegativeButton("NO", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// Canceled, do nothing.
			}
		});
    	
    	alert.show();
    }
    

    /**
     * Prompts the user to provide a filename that will be used
     * when writing the contents of the current timer event to.
     */
    private void _writeOutLapHistory() {
//		// TODO move this to the background service.
//		// Get the values needed for the output file.
//		Date timerStartedAt = new Date(boundService.getTimerStartTime());
//		
//		// Build the output string
//		final String out = "" +
//				"Started on: " + ((timerStartedAt!=null)?DateFormat.format("yyyy MM dd kk:mm:ss", timerStartedAt):"Unknown") + "\n" +
//				"Total Time: " + currTimeTxt.getText() + "\n" +
//				"Number of Laps: " + (lapCount-1) + "\n" + 
//				"**** TIMER HISTORY ****\n" +
//				"timerHistory";
//		
//		// Using the Simple file access util class output the text to a file
//		// while also prompting the user for the path and any confirmations
//		// needed.
//		new SimpleFileAccess().showOutFileAlertPromptAndWriteTo(this,
//				boundService.getAppPreferences().getOutfileRootPath(), out);
    }

    
    //////////////////////////////////////
    // Service Controls
    //////////////////////////////////////
    private boolean isSrvcbound = false;
    private BackgroundSrvc boundService;
    private ServiceConnection srvcConnection = new ServiceConnection() {
    	public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
    		boundService = ((BackgroundSrvc.LocalBinder)service).getService();
    		
    		// Provide the bound service with our timer UI listener
    		boundService.setUpdateUIListener(Main.this);
    		
    		// Update the app's default shared preferences 
    		_updateAppPreferences();
        	
        	// Refresh the UI's views
        	_refreshUI();
    	}

		@Override
		public void onServiceDisconnected(ComponentName name) {
	        // This is called when the connection with the service has been
	        // unexpectedly disconnected -- that is, its process crashed.
	        // Because it is running in our same process, we should never
	        // see this happen.
			boundService = null;
		}
    };
    
    
    /**
     * Create a connection to the background service.
     */
    private void _doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because we want a specific service implementation that
        // we know will be running in our own process (and thus won't be
        // supporting component replacement by other applications).
        bindService(new Intent(this, 
        		BackgroundSrvc.class), srvcConnection, Context.BIND_AUTO_CREATE);
        isSrvcbound = true;
    }
    
    
    /**
     * Tell the service to update it's preferences
     */
    private void _updateAppPreferences() {
    	if (isSrvcbound && boundService != null)
    		boundService.updateAppPreferences();
    }
    

    /**
     * Remove the connection to the background service.
     */
    private void _doUnbindService() {
        if (isSrvcbound) {
            // Detach our existing connection.
            unbindService(srvcConnection);
            isSrvcbound = false;
        }
    }
    
    
    ///////////////////////////////////
    // Message Handler
    ///////////////////////////////////
    private Handler myHandler = new Handler() {
    	@Override
    	public void handleMessage(Message msg) {
    		switch(msg.what) {
    		default:
    			break;
    		}
    	}
    };
    
    
    ////////////////////////////////////
    // Timer Service UI Update Listeners
    ////////////////////////////////////
	@Override
	public void setCurrentTime(final long currTime) {
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				currTimeTxt.setText(TextUtil.formatDateToString(currTime, numFormat));
				Main.this.currTime = currTime;
			}
		});
	}


	@Override
	public void setLapTime(final long lapTime) {
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				lapTimeTxt.setText(TextUtil.formatDateToString(lapTime, numFormat));
				Main.this.lapTime = lapTime;
			}
		});
	}

	
	@Override
	public void setLapCount(final int count) {
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				// The lap count is 0 based and needs to be offset for 
				// the user.
				lapCount = count+1;
				
				// Update the UI from the value provided.
				lapNumBtn.setText("Lap "+ Integer.toString(lapCount));
			}
		});
	}
	
	
	@Override
	public void resetUI() {
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				// Reset current timer and update text
				currTime = 0;
				currTimeTxt.setText(TextUtil.formatDateToString(currTime, numFormat));
				
				// Reset lap timer and update text
				lapTime = 0;
				lapTimeTxt.setText(TextUtil.formatDateToString(lapTime, numFormat));
		
				// Reset the timer history and update UI
				timerHistoryTxt.setText("");
			
				// Reset the lap count, and update the lap button
				lapCount = 1;
				lapNumBtn.setText("Lap "+ Integer.toString(lapCount));
			}
		});
	}

	
	@Override
	public void setTimerHistory(final String history) {
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				// Reset the timer history and update UI
				timerHistoryTxt.setText(history);
			}
		});
	}
}