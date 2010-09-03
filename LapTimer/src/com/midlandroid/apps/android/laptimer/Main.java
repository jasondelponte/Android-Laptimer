package com.midlandroid.apps.android.laptimer;

import java.text.NumberFormat;
import java.util.Date;
import java.util.List;

import com.midlandroid.apps.android.laptimer.background.BackgroundSrvc;
import com.midlandroid.apps.android.laptimer.background.timers.TimerUpdateUIListener;
import com.midlandroid.apps.android.laptimer.background.timers.TimerMode.RunningState;
import com.midlandroid.apps.android.laptimer.util.LapData;
import com.midlandroid.apps.android.laptimer.util.MessageId;
import com.midlandroid.apps.android.laptimer.util.SimpleFileAccess;
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
import android.text.format.DateFormat;
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
	private TextView lapListTxt;
	//private MenuItem timerModeMI;
	private MenuItem saveHistoryMI;
	
	// members
	private long currTime;
	private long lapTime;
	private int lapCount;
	
	private NumberFormat numFormat;
	private boolean keepTimerServiceAlive;
	private Messenger myMessenger;
	
	private String timerHistory;
	
	
    @Override
    /**
     * Called when the activity is first created.
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.main);
        
        currTime = lapTime = 0;
        lapCount = 1;
        
        // initialize the variables
        numFormat = NumberFormat.getInstance();
        numFormat.setMinimumIntegerDigits(2);
        numFormat.setMaximumIntegerDigits(2);
        numFormat.setParseIntegerOnly(true);
        keepTimerServiceAlive = false;
        
        myMessenger = new Messenger(myHandler);

    	// Get handles to each of the UI's elements
    	lapTimeTxt = (TextView)findViewById(R.id.lap_timer_txt);
    	lapTimeTxt.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (boundService.getAppPreferences().getUseOneClickTextCopy())
					_addViewTextToClipBoard(v);
			}
    	});
    	lapNumBtn = (Button)findViewById(R.id.lap_increment_btn);
    	lapNumBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				_lapIncrement();
			}
    	});
    	
    	currTimeTxt = (TextView)findViewById(R.id.timer_counter_txt);
    	currTimeTxt.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (boundService.getAppPreferences().getUseOneClickTextCopy())
					_addViewTextToClipBoard(v);
			}
    	});
    	startStopBtn = (Button)findViewById(R.id.timer_start_stop_btn);
    	startStopBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				_startStopTimer();
			}
    	});
    	
    	lapListTxt = (TextView)findViewById(R.id.lap_txt);
    	lapListTxt.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (boundService.getAppPreferences().getUseOneClickTextCopy())
					_addViewTextToClipBoard(v);
			}
    	});
    	timerHistory = "";
    }
    
    
    @Override
    public void onResume() {
    	super.onResume();
    	
    	// Refresh the UI's views
    	_refreshUI();
        
        // Attach to the service
        _doBindService();
    	
    	keepTimerServiceAlive = false;
    }
    
    
    @Override
    public void onPause() {
    	super.onPause();

    	keepTimerServiceAlive = true;
    	
    	// Disconnect from the service
    	_doUnbindService();
    }
    
    
    /////////////////////////////////
    // Overridden controls
    /////////////////////////////////
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if (keyCode == KeyEvent.KEYCODE_MENU && event.getRepeatCount() == 0) {
    		_setMenuItemEnabledBasedOnRunState();
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
    	
    	//_setMenuItemEnabledBasedOnRunState();
    	
		return true;
    }
    
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()) {
    	case R.id.mi_reset_timer:
    		_resetTimer();
    		return true;
    	case R.id.mi_save_lap_list:
    		_showOutFileAlertPrompt();
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
    
    
    ////////////////////////////////////
    // User Action Handler Methods
    ////////////////////////////////////
    /**
     * Notifies the background service that the user has selected
     * to start or stop the timer.
     */
    private void _startStopTimer() {
    	keepTimerServiceAlive = true;
    	_msgTimerService(MessageId.CMD_START_STOP_TIMER);
    }
    
    
	/**
	 * Notifies the background service that the lap count should
	 * be incremented.
	 */
    private void _lapIncrement() {
    	_msgTimerService(MessageId.CMD_LAP_INCREMENT);
    }
    
    
    /**
     * Resets the timer.  If the user has specified to be warned when 
     * reseting the timer prompt them instead.
     */
    private void _resetTimer() {
    	if (boundService.getAppPreferences().getUseSecondChanceReset()) {
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
    	keepTimerServiceAlive = false;
    	_msgTimerService(MessageId.CMD_RESET_TIMER);
    }
    
    
    /**
     * Notifies the background service that this UI needs to be updated.
     */
    private void _refreshUI() {
    	lapListTxt.setText("");
    	_msgTimerService(MessageId.CMD_REFRESH_MAIN_UI);
    }
    

    ////////////////////////////////////
    // Private methods
    ////////////////////////////////////
    /**
     * Adds the text of the provided view to the android system's
     * clipboard buffer.
     * @param v View with text to be added to the clipboard
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
		//timerModeMI.setEnabled(boundService.getTimerState() != RunningState.RUNNING);
		saveHistoryMI.setEnabled(boundService.getTimerState() != RunningState.RUNNING);
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
     * the provided payload
     * @param cmd Message id
     * @param payload Data to be send to background service
     */
    private void _msgTimerService(final int cmd, final Object payload) {
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
    
    
    /**
     * Displays the Android Preferences UI to the user.
     */
    private void _showPreferences() {
		keepTimerServiceAlive = true;
		
    	Intent i = new Intent(this, Preferences.class);
    	startActivity(i);
    }
    
    
    /**
     * Displays the timer mode selection UI 
     */
    private void _showTimerModeSelection() {
		keepTimerServiceAlive = true;
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
    private void _showOutFileAlertPrompt() {
		// Get the values needed for the output file.
		String timerMode = boundService.getTimerModeName();
		Date timerStartedAt = new Date(boundService.getTimerStartTime());
		
		// Build the output string
		final String out = "" +
				"Timer Mode: " + timerMode + "\n" +
				"Started on: " + ((timerStartedAt!=null)?DateFormat.format("yyyy MM dd kk:mm:ss", timerStartedAt):"Unknown") + "\n" +
				"Total Time: " + currTimeTxt.getText() + "\n" +
				"Number of Laps: " + (lapCount-1) + "\n" + 
				"**** LAP HISTORY ****\n" +
				lapListTxt.getText();
		
		// Using the Simple file access util class output the text to a file
		// while also prompting the user for the path and any confirmations
		// needed.
		new SimpleFileAccess().showOutFileAlertPromptAndWriteTo(this,
				boundService.getAppPreferences().getOutfileRootPath(), out);
    }

    
    //////////////////////////////////////
    // Service Controls
    //////////////////////////////////////    
//    private void _disconnectFromService() {
//    	TimerService srvc = TimerService.getService();
//    	if (srvc!=null) {
//    		srvc.setUpdateUIListener(null);
//    		if (srvc.getState()==RunningState.RESETTED && 
//    				keepTimerServiceAlive==false) {
//		    	Intent i = new Intent(this, TimerService.class);
//		    	stopService(i);
//    		}
//    	}
//    }
    
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
    		
    		// Update the app's default shared preferences 
    		_updateAppPreferences();
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
    		case MessageId.CMD_CLEAR_TIMER_HISTORY:
    			timerHistory = "";
    			lapListTxt.setText(timerHistory);
    			break;
    		}
    	}
    };
    
    
    ////////////////////////////////////
    // Timer Service UI Update Listeners
    ////////////////////////////////////
	@Override
	public void updateCurrentTime(final long currTime) {
//		this.runOnUiThread(new Runnable() {
//			@Override
//			public void run() {
				currTimeTxt.setText(TextUtil.formatDateToString(currTime, numFormat));
				Main.this.currTime = currTime;
//			}
//		});
	}


	@Override
	public void updateLapTime(final long lapTime) {
//		this.runOnUiThread(new Runnable() {
//			@Override
//			public void run() {
				currTimeTxt.setText(TextUtil.formatDateToString(lapTime, numFormat));
				Main.this.lapTime = lapTime;
//			}
//		});
	}


	@Override
	public void updateLapIncrement(final long currTime, final long lapTime) {
//		this.runOnUiThread(new Runnable() {
//			@Override
//			public void run() {
				// Create a line of text recording the lap increment
				String item = "Lap " + Integer.toString(lapCount) + ": " +
						TextUtil.formatDateToString(lapTime, numFormat) + " - " +
						"Total: "+TextUtil.formatDateToString(currTime, numFormat) +
						"\n";
				
				// Add the item to the beginning of the history
				timerHistory = item + timerHistory;
				lapListTxt.setText(timerHistory);
				
				// Increment the lap count, and update the lap button
				lapCount++;
				lapNumBtn.setText("Lap "+ Integer.toString(lapCount));
//			}
//		});
	}


	@Override
	public void updateLapList(final List<LapData> laps) {
		// TODO Auto-generated method stub
		
	}
}