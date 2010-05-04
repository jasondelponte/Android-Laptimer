package com.midlandroid.apps.android.timerwithsetcounter;

import java.text.NumberFormat;

import com.midlandroid.apps.android.timerwithsetcounter.timerservice.LapData;
import com.midlandroid.apps.android.timerwithsetcounter.timerservice.TimerService;
import com.midlandroid.apps.android.timerwithsetcounter.timerservice.mode.TimerMode.RunningState;
import com.midlandroid.apps.android.timerwithsetcounter.timerservice.uilistener.TimerUpdateUIListener;
import com.midlandroid.apps.android.timerwithsetcounter.util.MessageId;
import com.midlandroid.apps.android.timerwithsetcounter.util.TextUtil;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class Main extends Activity implements TimerUpdateUIListener {	
	// Views
	private Button startStopBtn;
	private Button lapNumBtn;
	private TextView lapTimeTxt;
	private TextView currTimeTxt;
	private ListView lapList;
	private MenuItem timerModeMI;
	// members
	private ArrayAdapter<String> lapListAdapater;
	private NumberFormat numFormat;
	private boolean keepTimerServiceAlive;
	private Messenger myMessenger;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // Init
        numFormat = NumberFormat.getInstance();
        numFormat.setMinimumIntegerDigits(2);
        numFormat.setMaximumIntegerDigits(2);
        numFormat.setParseIntegerOnly(true);
        keepTimerServiceAlive = false;
        myMessenger = new Messenger(myHandler);

        _createService();
    	
    	lapTimeTxt = (TextView)findViewById(R.id.lap_timer_txt);
    	lapNumBtn = (Button)findViewById(R.id.lap_increment_btn);
    	lapNumBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				_setIncrement();
			}
    	});
    	
    	currTimeTxt = (TextView)findViewById(R.id.timer_counter_txt);
    	startStopBtn = (Button)findViewById(R.id.timer_start_stop_btn);
    	startStopBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
		    	_connectToService();
				_startStopTimer();
			}
    	});
    	
    	lapList = (ListView)findViewById(R.id.lap_list);    	
    	lapListAdapater = new ArrayAdapter<String>(this,
    			R.layout.lap_list_text_item);
    	lapList.setAdapter(lapListAdapater);
    	
    	_connectToService();
    	_refreshUI();
    }
    
    @Override
    public void onResume() {
    	super.onResume();

        _createService();
    	_connectToService();
    	
    	keepTimerServiceAlive = false;
    }
    
    @Override
    public void onPause() {
    	super.onPause();

    	keepTimerServiceAlive = true;
    	
    	_disconnectFromService();
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	
    	_disconnectFromService();
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
    		_setTimerModeMIEnabled();
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
    	
    	_setTimerModeMIEnabled();
    	
		return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()) {
    	case R.id.mi_reset_timer:
    		_resetTimer();
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
		
		lapListAdapater.insert(item, 0);
	}

	@Override
	public void clearLapList() {
		lapListAdapater.clear();
	}

	@Override
	public void updateTimerUI(final long currTime, final long lapTime, final int setCount) {
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				currTimeTxt.setText(TextUtil.formatDateToString(currTime,numFormat));
				lapTimeTxt.setText(TextUtil.formatDateToString(lapTime,numFormat));
				lapNumBtn.setText("Lap "+Integer.valueOf(setCount).toString());
			}
		});
	}
    
    
    ////////////////////////////////////
    // User Action Handler Methods
    ////////////////////////////////////
    private void _startStopTimer() {
    	keepTimerServiceAlive = true;
    	_msgTimerService(MessageId.MainCmd.CMD_START_STOP_TIMER);
    }
    
    private void _setIncrement() {
    	_msgTimerService(MessageId.MainCmd.CMD_SET_INCREMENT);
    }
    
    private void _resetTimer() {
    	keepTimerServiceAlive = false;
    	_msgTimerService(MessageId.MainCmd.CMD_RESET_TIMER);
    }
    
    private void _preferencesChanged() {
    	_msgTimerService(MessageId.MainCmd.CMD_PREFERENCES_CHANGED);
    }
    

    ////////////////////////////////////
    // Private methods
    ////////////////////////////////////
    private void _setTimerModeMIEnabled() {
    	TimerService srvc = TimerService.getService();
    	if (srvc!=null && timerModeMI!=null)
    		timerModeMI.setEnabled(srvc.getState()!=RunningState.RUNNING);
    	
    }
//    private void _updateTimerStartStopBtn() {
//    	TimerService srvc = TimerService.getService();
//    	if (srvc!=null) {
//    		if (srvc.getState()==RunningState.RUNNING)
//    			startStopBtn.setText(getResources().getString(R.string.timer_stop_btn_txt));
//    		else
//    			startStopBtn.setText(getResources().getString(R.string.timer_start_btn_txt));
//    	}
//    }
    
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

    
    //////////////////////////////////////
    // Service Controls
    //////////////////////////////////////
    private void _createService() {
        if (TimerService.getService()==null) {
			Intent i = new Intent(this, TimerService.class);
			startService(i);
        }
    }
    
    private void _connectToService() {
    	TimerService srvc = TimerService.getService();
    	if (srvc!=null) {
    		srvc.setUpdateUIListener(this);
    	}
    }
    
    private void _disconnectFromService() {
    	TimerService srvc = TimerService.getService();
    	if (srvc!=null) {
    		srvc.setUpdateUIListener(null);
    		if (srvc.getState()==RunningState.RESETTED && 
    				keepTimerServiceAlive==false) {
		    	Intent i = new Intent(this, TimerService.class);
		    	stopService(i);
    		}
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