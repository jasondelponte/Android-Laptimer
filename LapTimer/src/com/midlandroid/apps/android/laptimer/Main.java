package com.midlandroid.apps.android.laptimer;

import java.text.NumberFormat;
import java.util.Date;

import com.midlandroid.apps.android.laptimer.background.BackgroundSrvc;
import com.midlandroid.apps.android.laptimer.timerservice.LapData;
import com.midlandroid.apps.android.laptimer.timerservice.TimerService;
import com.midlandroid.apps.android.laptimer.timerservice.mode.TimerMode.RunningState;
import com.midlandroid.apps.android.laptimer.timerservice.uilistener.TimerUpdateUIListener;
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
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.preference.PreferenceManager;
import android.text.ClipboardManager;
import android.text.format.DateFormat;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class Main extends Activity implements TimerUpdateUIListener {	
	// Views
	private Button startStopBtn;
	private Button lapNumBtn;
	private TextView lapTimeTxt;
	private TextView currTimeTxt;
	private TextView lapListTxt;
	private MenuItem timerModeMI;
	private MenuItem saveHistoryMI;
	// members
	private Integer lapCount;
	private NumberFormat numFormat;
	private boolean keepTimerServiceAlive;
	private Messenger myMessenger;
	private String lapStrings;
	private boolean isOneClickTextCopy;
	private boolean secondChanceReset;
	private String outfileRootPath;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        isOneClickTextCopy = false;
        lapCount = 1;
        
        // Init
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
				if (isOneClickTextCopy)
					_addViewTextToClipBoard(v);
			}
    	});
    	lapNumBtn = (Button)findViewById(R.id.lap_increment_btn);
    	lapNumBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				_setIncrement();
			}
    	});
    	
    	currTimeTxt = (TextView)findViewById(R.id.timer_counter_txt);
    	currTimeTxt.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isOneClickTextCopy)
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
				if (isOneClickTextCopy)
					_addViewTextToClipBoard(v);
			}
    	});
    	lapStrings = "";
    }
    
    @Override
    public void onStart() {
    	super.onStart();

    	_refreshUI();
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	
    	// Update the preferences
    	_getPreferences();
        
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
    
    @Override
    public void onDestroy() {
    	super.onDestroy();

    	// Disconnect from the service
    	_doUnbindService();
    }
    
    @Override
    protected void onActivityResult(int reqCode, int resCode, Intent data) {
    	if (reqCode == 0) {
    		if (resCode == RESULT_OK) {
    			
    		}
    	}
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
    	
    	timerModeMI = menu.findItem(R.id.mi_timer_mode);
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
    		_showOutFileAlertPrompt();
    		return true;
    	case R.id.mi_preferences:
    		_showPreferences();
    		_preferencesChanged();
    		return true;
    	case R.id.mi_timer_mode:
    		_showTimerModeSelection();
    		return true;
    	}
    	return false;
    }
    
    
    ////////////////////////////////////
    // Timer Service UI Update Listeners
    ////////////////////////////////////
	@Override
	public void addLapToUI(final LapData lapData) {
		String item = "Lap "+lapData.getLapNum()+": "+
				TextUtil.formatDateToString(lapData.getLapTime(), numFormat)+" - "+
				"Total: "+TextUtil.formatDateToString(lapData.getTotalTime(), numFormat);
		
		if (lapData.getLapNum()>1)
			item += "\n";
		
		lapStrings = item + lapStrings;
		lapListTxt.setText(lapStrings);
	}

	@Override
	public void clearLapList() {
		lapStrings = "";
		lapListTxt.setText(lapStrings);
	}

	@Override
	public void updateTimerUI(final long currTime, final long lapTime, final int setCount) {
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				lapCount = setCount;
				currTimeTxt.setText(TextUtil.formatDateToString(currTime,numFormat));
				lapTimeTxt.setText(TextUtil.formatDateToString(lapTime,numFormat));
				lapNumBtn.setText("Lap "+ lapCount.toString());
			}
		});
	}
    
    
    ////////////////////////////////////
    // User Action Handler Methods
    ////////////////////////////////////
    private void _setIncrement() {
    	_msgTimerService(MessageId.MainCmd.CMD_SET_INCREMENT);
    }
    
    private void _resetTimer() {
    	if (secondChanceReset) {
    		_showPromptToResetTimer();
    	} else {
    		_doResetTimer();
    	}
    }
    
    private void _doResetTimer() {
    	keepTimerServiceAlive = false;
    	_msgTimerService(MessageId.MainCmd.CMD_RESET_TIMER);
    }
    
    private void _preferencesChanged() {
    	_msgTimerService(MessageId.MainCmd.CMD_PREFERENCES_CHANGED);
    }
    

    ////////////////////////////////////
    // Private methods
    ////////////////////////////////////
    private void _startStopTimer() {
    	keepTimerServiceAlive = true;
    	_msgTimerService(MessageId.MainCmd.CMD_START_STOP_TIMER);
    }
    
    private void _getPreferences() {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		Resources res = getResources();
		
		// Use delay timer on restarts?
		isOneClickTextCopy = prefs.getBoolean(
				res.getString(R.string.pref_one_click_txt_cpy_key), true);
		
		outfileRootPath="";
		//outfileRootPath = prefs.getString(res.getString(R.string.pref_outfile_root_key), "/sdcard/");
		
		secondChanceReset = prefs.getBoolean(res.getString(R.string.pref_second_chance_reset_key), true);
    }
    
    private void _addViewTextToClipBoard(View v) {
		 ClipboardManager clipboard = 
		      (ClipboardManager) getSystemService(CLIPBOARD_SERVICE); 

		 clipboard.setText(((TextView)v).getText());
		 Toast t = Toast.makeText(this, R.string.text_copied_to_clipboard, Toast.LENGTH_SHORT);
		 t.show();
    }
    
    private void _setMenuItemEnabledBasedOnRunState() {
    	TimerService srvc = TimerService.getService();
    	if (srvc!=null && timerModeMI!=null) {
    		//timerModeMI.setEnabled(srvc.getState()!=RunningState.RUNNING);
    		saveHistoryMI.setEnabled(srvc.getState()!=RunningState.RUNNING);
    	}
    }
    
    private void _msgTimerService(final int cmd) {
    	_msgTimerService(cmd, null);
    }
    
    private void _msgTimerService(final int cmd, final Object payload) {
    	TimerService srvc = TimerService.getService();
    	if (srvc!=null) {
        	// Get the handler
        	Handler handler = srvc.getMessageHandler();
        	// Start the timer
        	Message msg = Message.obtain();
        	msg.arg1 = MessageId.SRC_MAIN;
        	msg.arg2 = cmd;
        	msg.obj = payload;
        	msg.replyTo = myMessenger;
        	handler.sendMessage(msg);
    	}
    }
    
    private void _refreshUI() {
    	lapListTxt.setText("");
    	_msgTimerService(MessageId.MainCmd.CMD_REFRESH);
    }
    
    private void _showPreferences() {
		keepTimerServiceAlive = true;
		
    	Intent i = new Intent(this, Preferences.class);
    	startActivity(i);
    }
    
    private void _showTimerModeSelection() {
		keepTimerServiceAlive = true;
		
    }
    
    public static final int SHOW_TIMER_DELAY_ACT = 0;
    private void _showTimerDelayUI() {
    	Intent i = new Intent(this, DelayTimeCountDown.class);
    	startActivityForResult(i, SHOW_TIMER_DELAY_ACT);
    }
    
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
        
    private void _showOutFileAlertPrompt() {
			// Get the values needed for the output file.
			String timerMode = "Unknown Mode";
			Date timerStartedAt = new Date();
			TimerService srvc = TimerService.getService();
	    	if (srvc!=null) {
	    		timerMode = srvc.getTimerModeName();
	    		timerStartedAt = srvc.getTimerStartedAt();
	    	}
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
			new SimpleFileAccess().showOutFileAlertPromptAndWriteTo(this, outfileRootPath, out);
    }

    
    //////////////////////////////////////
    // Service Controls
    //////////////////////////////////////
//    private void _createService() {
//        if (TimerService.getService()==null) {
//			Intent i = new Intent(this, TimerService.class);
//			startService(i);
//        }
//    }
//    
//    private void _connectToService() {
//    	TimerService srvc = TimerService.getService();
//    	if (srvc!=null) {
//    		srvc.setUpdateUIListener(this);
//    	}
//    }
//    
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
    		switch(msg.arg1) {
    		case MessageId.SRC_TIMERSERVICE:
    			switch(msg.arg2) {
    			case MessageId.TimerServiceCmd.CMD_SHOW_TIMER_DELAY_UI:
        			_showTimerDelayUI();
    				break;
    			}
    			break;
    		}
    	}
    };
}