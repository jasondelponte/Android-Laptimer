package com.midlandroid.apps.android.timerwithsetcounter.timerservice;

import java.util.Date;

import com.midlandroid.apps.android.timerwithsetcounter.R;
import com.midlandroid.apps.android.timerwithsetcounter.timerservice.mode.DelayTimer;
import com.midlandroid.apps.android.timerwithsetcounter.timerservice.mode.SimpleCountUp;
import com.midlandroid.apps.android.timerwithsetcounter.timerservice.mode.TimerMode;
import com.midlandroid.apps.android.timerwithsetcounter.timerservice.mode.TimerMode.RunningState;
import com.midlandroid.apps.android.timerwithsetcounter.timerservice.uilistener.DelayTimerUpdateUIListener;
import com.midlandroid.apps.android.timerwithsetcounter.timerservice.uilistener.TimerUpdateUIListener;
import com.midlandroid.apps.android.timerwithsetcounter.util.MessageId;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

public class TimerService extends Service {
	private static final String LOG_TAG = TimerService.class.getSimpleName();
	private static TimerService timerService = null;

	// Current Timers
	private TimerMode timerMode;
	private Messenger mainUIMsgr;
	private Messenger delayTimerMsgr;
	
	private Date timeStartedAt;
	
	// Preferences
	private boolean preferencesChanged;
	private int timerStartDelay;
	private boolean useDelayTimer;
	private boolean useDelayTimerOnRestarts;
	private boolean useAudioAlerts;
	private boolean useWakeLock;
	
	// Delay Timer
	private DelayTimer delayTimer;
	
	// power manger wake lock
	PowerManager.WakeLock wakeLock;
	
	/////////////////////////////////
	// Overridden Service Methods
	/////////////////////////////////
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		timerService = this;
		
		preferencesChanged = true;
		timerStartDelay = 0;
		useDelayTimer = false;
		useDelayTimerOnRestarts = false;
		useAudioAlerts = false;
		useWakeLock = false;

		delayTimer = new DelayTimer(myHandler);
		
		// Default timer mode
		timerMode = new SimpleCountUp();
				
		_getPrefs();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		timerService = null;
		
		_killMainTimer();
		_killDelayTimer();
	}
	

	/////////////////////////////////////////
	// Public methods
	/////////////////////////////////////////
	public void setUpdateUIListener(TimerUpdateUIListener uiUpdateListener) {
		timerMode.setUpdateUIListener(uiUpdateListener);
	}
	
	public TimerUpdateUIListener getServiceUpdateUIListener() {
		return timerMode.getUpdateUIListener();
	}
	
	public void setDelayTimerUIListener(DelayTimerUpdateUIListener delayTimeCountDown) {
		delayTimer.setUpdateUIListener(delayTimeCountDown);
	}
	
	public DelayTimerUpdateUIListener getDelayTimerUIListner() {
		return delayTimer.getUpdateUIListener();
	}
	
	public void setDelayTimerMessenger(Messenger msgr) {
		delayTimerMsgr = msgr;
	}
	
	public Messenger getDelayTimerMessenger() {
		return delayTimerMsgr;
	}
	
	public RunningState getState() {
		return timerMode.getState();
	}
	
	public String getTimerModeName() {
		return timerMode.getTimerModeName();
	}
	
	public Date getTimerStartedAt() {
		return timeStartedAt;
	}
	

	/////////////////////////////////////////
	// Message Processor 
	/////////////////////////////////////////
	private Handler myHandler = new Handler() {
    	@Override
    	public void handleMessage(Message msg) {
    		if (preferencesChanged)
    			_getPrefs();
    		
    		switch(msg.arg1) {
    		case MessageId.SRC_MAIN:
    			_handleMainCmds(msg);
    			break;
    		case MessageId.SRC_DELAYTIMECOUNTDOWN:
    			_handleDelayTimeCountDownCmd(msg);
    			break;
    		case MessageId.SRC_DELAYTIMER:
    			_handleDelayTimerCmd(msg);
    			break;
    		}
    	}
	};
	
	private void _handleMainCmds(Message msg) {
		// Save off the reply to handler
		mainUIMsgr = msg.replyTo;
		
		switch(msg.arg2) {
		case MessageId.MainCmd.CMD_START_STOP_TIMER:
			if (timerMode.getState()!=RunningState.RUNNING){
				if (useDelayTimerOnRestarts || useDelayTimer) {
					_startDelayTimer();
				} else {
					_startMainTimer();
					_grabWakeLock();
				}
			} else {
				_stopMainTimer();
				_releaseWakeLock();
			}
			break;
		case MessageId.MainCmd.CMD_RESET_TIMER:
			_getPrefs();
			timeStartedAt = null;
			timerMode.resetTimer();
			delayTimer.stopTimer();
			_releaseWakeLock();
			break;
		case MessageId.MainCmd.CMD_SET_INCREMENT:
			timerMode.lapTimer();
			break;
		case MessageId.MainCmd.CMD_REFRESH:
			timerMode.refreshUI();
			break;
		case MessageId.MainCmd.CMD_PREFERENCES_CHANGED:
			preferencesChanged = true;
			break;
		}
	}

	private void _handleDelayTimeCountDownCmd(Message msg) {
		switch(msg.arg2) {
		case MessageId.DelayTimerCountDownCmd.CMD_STOP_TIMER:
			_stopDelayTimer();
			break;
		}
	}
	
	private void _handleDelayTimerCmd(Message msg) {
		switch(msg.arg2) {
		case MessageId.DelayTimerCmd.CMD_TIMER_FINISHED:
			useDelayTimer = false;
			
			_stopDelayTimer();
			
			if (useAudioAlerts)
				_playTimerStartAudio();
			
			_finishDelayTimerUI();
			_startMainTimer();
			break;
		}
	}
	
	private void _startDelayTimer() {
		_replyToMessager(MessageId.TimerServiceCmd.CMD_SHOW_TIMER_DELAY_UI, mainUIMsgr);
		delayTimer.startTimer(timerStartDelay);
	}
	
	private void _startMainTimer() {
		timeStartedAt = new Date();
		timerMode.startTimer();
	}
	
	private void _finishDelayTimerUI() {
		if (delayTimerMsgr!=null)
			_replyToMessager(MessageId.TimerServiceCmd.CMD_FINISH_TIMER_DELAY_UI, delayTimerMsgr);
	}

	///////////////////////////////
	// Private Methods
	///////////////////////////////
	private void _replyToMessager(final int cmd, Messenger replyTo) {
		_replyToMessage(cmd, null, replyTo);
	}
	
	private void _replyToMessage(final int cmd, final Object payload, Messenger replyTo) {
		Message msg = Message.obtain();
		msg.arg1 = MessageId.SRC_TIMERSERVICE;
		msg.arg2 = cmd;
		try {
			replyTo.send(msg);
		} catch (RemoteException e) {
			Log.e("TimerService._replyToMessage", e.getMessage(), e.getCause());
		}
	}
	
	private void _getPrefs() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		Resources res = getResources();
		
		// Delay timer amount
		try {
			timerStartDelay = Integer.valueOf(prefs.getString(
					res.getString(R.string.pref_timer_start_delay_key), "0"))*1000;
		} catch (NumberFormatException e) {
			timerStartDelay = 0;
		}		
		// Should the delay timer be used?
		useDelayTimer = (timerStartDelay > 0);
		
				
		// Use delay timer on restarts?
		useDelayTimerOnRestarts = prefs.getBoolean(
				res.getString(R.string.pref_timer_start_delay_on_restarts_key), false);
		
		
		// Should Audio alerts be played?
		useAudioAlerts = false;
		
		preferencesChanged = false;
	}
	
	/**
	 * Grab the wake lock preventing the screen from timing out
	 * if the option is set
	 */
	private void _grabWakeLock() {
		if (useWakeLock) {
			if (wakeLock != null) {
				PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
				wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, LOG_TAG);
			}
			
			if (!wakeLock.isHeld())
				wakeLock.acquire();
		}
	}
	
	/**
	 * Releases the previously grabbed wake lock
	 */
	private void _releaseWakeLock() {
		if (useWakeLock && wakeLock != null && wakeLock.isHeld()) {
			wakeLock.release();
		}
	}
	
	private void _playTimerStartAudio() {
		// TODO Fix me
//		MediaPlayer mp = new MediaPlayer();
//		AssetManager am = getResources().getAssets();
//		try {
//			mp.setDataSource(am.openFd("buzzer1.wav").getFileDescriptor());
//		} catch (IllegalArgumentException e) {
//			e.printStackTrace();
//		} catch (IllegalStateException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		mp.start();
	}
	
	
	////////////////////////////////////////
	// Timer Controls
	////////////////////////////////////////
	private void _stopMainTimer() {
		timerMode.stopTimer();
	}
	
	private void _killMainTimer() {
		timerMode.killTimer();
	}
	
	private void _stopDelayTimer() {
		delayTimer.stopTimer();
	}
	
	private void _killDelayTimer() {
		delayTimer.killTimer();
	}
	
	
	////////////////////////////////////////
	// Public static methods
	////////////////////////////////////////
	public static TimerService getService() {
		return timerService;
	}
	
	public Handler getMessageHandler() {
		return myHandler;
	}
}
