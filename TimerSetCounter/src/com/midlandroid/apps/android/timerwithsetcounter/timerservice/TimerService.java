package com.midlandroid.apps.android.timerwithsetcounter.timerservice;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.midlandroid.apps.android.timerwithsetcounter.R;
import com.midlandroid.apps.android.timerwithsetcounter.R.string;
import com.midlandroid.apps.android.timerwithsetcounter.util.MessageId;
import com.midlandroid.apps.android.timerwithsetcounter.util.MessageId.DelayTimerCountDownCmd;
import com.midlandroid.apps.android.timerwithsetcounter.util.MessageId.MainCmd;
import com.midlandroid.apps.android.timerwithsetcounter.util.MessageId.TimerServiceCmd;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

public class TimerService extends Service {
	private static final int TIMER_UPDATE_RATE = 100;
	
	public  static enum RunningState {
		RUNNING, STOPPED, RESETTED, TIMER_DELAY
	}
	
	private static TimerService timerService = null;
	
	private TimerServiceUpdateUIListener uiUpdateListener;
	private TimerServiceUpdateUIListener uiDelayTimerListener;
	private RunningState state;
	private List<LapData> lapDataList;
	private int timerStartDelay;
	private Timer timer;
	private Timer delayTimer;
	private long initTime;
	private long prevTime;
	private long currTime;
	private long delayTime;
	private long delayInitTime;
	private boolean useDelayTimerOnRestarts;
	private long pausedAtTime;
	private int setCount;
	private boolean preferencesChanged;
	private Messenger delayTimerMsgr;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		timerService = this;
		state = RunningState.RESETTED;
		lapDataList = new ArrayList<LapData>();
		uiUpdateListener = null;
		uiDelayTimerListener = null;
		timer = null;
		delayTimer = null;
		initTime = 0;
		prevTime = 0;
		currTime = 0;
		delayInitTime = 0;
		useDelayTimerOnRestarts = false;
		pausedAtTime = 0;
		setCount = 1;
		preferencesChanged = true;
		
		_getPrefs();
		delayTime = timerStartDelay;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		timerService = null;
		
		_shutdownTimer();
		_shutdownDelayTimer();
	}
	
	public static TimerService getService() {
		return timerService;
	}
	
	public Handler getMessageHandler() {
		return myHandler;
	}
	
	public void setUpdateUIListener(TimerServiceUpdateUIListener uiUpdateListener) {
		this.uiUpdateListener = uiUpdateListener;
	}
	
	public void setDelayTimerUIListener(TimerServiceUpdateUIListener uiDelayTimerListener) {
		this.uiDelayTimerListener = uiDelayTimerListener;
	}
	
	public TimerServiceUpdateUIListener getServiceUpdateUIListener() {
		return uiUpdateListener;
	}
	
	public TimerServiceUpdateUIListener getDelayTimerUIListner() {
		return uiDelayTimerListener;
	}
	
	public void setDelayTimerMessenger(Messenger delayTimerMsgr) {
		this.delayTimerMsgr = delayTimerMsgr;
	}
	
	public Messenger getDelayTimerMessenger() {
		return delayTimerMsgr;
	}
	
	public RunningState getState() {
		return state;
	}
	
	private Handler myHandler = new Handler() {
    	@Override
    	public void handleMessage(Message msg) {
    		switch(msg.arg1) {
    		case MessageId.SRC_MAIN:
    			_handleMainCmds(msg);
    			break;
    		case MessageId.SRC_DELAYTIMECOUNTDOWN:
    			_handleDelayTimeCountDownCmd(msg);
    			break;
    		}
    	}
	};
	
	private void _handleMainCmds(Message msg) {
		switch(msg.arg2) {
		case MessageId.MainCmd.CMD_START_STOP_TIMER:
			if (preferencesChanged) {
				_getPrefs();
			}
			
			if (state!=RunningState.RUNNING&&state!=RunningState.TIMER_DELAY){
				if (useDelayTimerOnRestarts || (delayTime > 0))
					_startDelayTimer(msg.replyTo);
				else
					_startTimer();
			} else {
				_stopTimer();
			}
			break;
		case MessageId.MainCmd.CMD_RESET_TIMER:
			_resetTimer();
			break;
		case MessageId.MainCmd.CMD_SET_INCREMENT:
			_setIncrement();
			break;
		case MessageId.MainCmd.CMD_REFRESH:
			_refreshUI();
			break;
		case MessageId.MainCmd.CMD_PREFERENCES_CHANGED:
			preferencesChanged = true;
			break;
		}
	}

	private void _handleDelayTimeCountDownCmd(Message msg) {
		switch(msg.arg2) {
		case MessageId.DelayTimerCountDownCmd.CMD_STOP_TIMER:
			_stopTimer();
			break;
		}
	}
	
	private void _startDelayTimer(Messenger msgr) {
		if (state!=RunningState.TIMER_DELAY) {
			_replyToMessage(MessageId.TimerServiceCmd.CMD_SHOW_TIMER_DELAY_UI, msgr);
			
			delayInitTime = 0;
			delayTime = timerStartDelay;
			_setupDelayTimer();
			
			state = RunningState.TIMER_DELAY;
		}
	}
	
	private void _startTimer() {
		if (state!=RunningState.RUNNING) {
			_setupUpCountTimer();
			
			state = RunningState.RUNNING;
		}
	}
		
	private void _stopTimer() {
		if (state==RunningState.RUNNING || state==RunningState.TIMER_DELAY) {
			pausedAtTime = currTime;
			_shutdownTimer();
			_shutdownDelayTimer();
			
			state = RunningState.STOPPED;
		}
	}
	
	private void _resetTimer() {	
		_stopTimer();
		
		state = RunningState.RESETTED;
		initTime = 0;
		currTime = 0;
		prevTime = 0;
		delayTime = timerStartDelay;
		delayInitTime = 0;
		pausedAtTime = 0;
		setCount = 1;
		lapDataList.clear();
		
		_updateUITimer(currTime, currTime-prevTime, setCount);
		_updateUIClearLaps();
	}
	
	private void _setIncrement() {
		if (state==RunningState.RUNNING) {
			final LapData lapData = new LapData(setCount, currTime-prevTime, currTime);
			lapDataList.add(lapData);
			
			_updateUIAddLap(lapData);
	
			setCount++;
			prevTime = currTime;
		}
	}
	
	private void _refreshUI() {
		_updateUITimer(currTime, currTime-prevTime, setCount);
		
		for (LapData lapData : lapDataList) {
			_updateUIAddLap(lapData);
		}
	}
	
	private void _updateUITimer(final long currTime, final long lapTime, final int setCount) {
		if (uiUpdateListener!=null) {
			uiUpdateListener.updateTimerUI(currTime, lapTime, setCount);
		}
	}
	
	private void _updateUIAddLap(final LapData lapData) {
		if (uiUpdateListener!=null) {
			uiUpdateListener.addLapToUI(lapData);
		}
	}
	
	private void _updateUIClearLaps() {
		if (uiUpdateListener!=null)
			uiUpdateListener.clearLapList();
	}
	
	private void _updateDelayTimerUI(final long currTime) {
		if (uiDelayTimerListener!=null) {
			uiDelayTimerListener.updateTimerUI(currTime, 0, 0);
		}
	}
	
	private void _finishDelayTimerUI() {
		if (uiDelayTimerListener!=null) {
			_replyToMessage(MessageId.TimerServiceCmd.CMD_FINISH_TIMER_DELAY_UI, delayTimerMsgr);
		}
	}
	
	private void _replyToMessage(final int cmd, Messenger replyTo) {
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
		SharedPreferences prefs = PreferenceManager
			.getDefaultSharedPreferences(getBaseContext());
		
		Resources res = getResources();
		
		try {
			timerStartDelay = Integer.valueOf(prefs.getString(
					res.getString(R.string.pref_timer_start_delay_key), "0"))*1000;
		} catch (NumberFormatException e) {
			timerStartDelay = 0;
		}
		delayTime = timerStartDelay;
				
		useDelayTimerOnRestarts = prefs.getBoolean(
				res.getString(R.string.pref_timer_start_delay_on_restarts_key), false);
		
		preferencesChanged = false;
	}
	
	private void _playTimerStartAudio() {
//		MediaPlayer mp = new MediaPlayer();
//		AssetManager am = getResources().getAssets();
//		try {
//			mp.setDataSource(am.openFd("buzzer1.wav").getFileDescriptor());
//		} catch (IllegalArgumentException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IllegalStateException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		mp.start();
	}
	
	private void _setupDelayTimer() {
		synchronized(this) {
			if (delayTimer==null) {
				delayTimer = new Timer();
				delayTimer.schedule(new TimerTask() {
					@Override
					public void run() {
						long schTime = this.scheduledExecutionTime();
						
						if (delayInitTime==0)
							delayInitTime = schTime;
						
						delayTime = timerStartDelay - (schTime-delayInitTime);
						_updateDelayTimerUI(delayTime+1000);
						
						if (delayTime <= 0) {
							cancel();
							_finishDelayTimerUI();
							_playTimerStartAudio();
							_startTimer();
						}
					}
				}, 0, TIMER_UPDATE_RATE);
			}
		}
	}
		
	private void _setupUpCountTimer() {
		synchronized(this) {
			if (timer==null) {
				timer = new Timer();
				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						long schTime = this.scheduledExecutionTime();
						
						if (initTime==0)
							initTime = schTime;
						
						if (pausedAtTime!=0) {
							initTime += (schTime - (initTime+pausedAtTime));						
							pausedAtTime = 0;
						}

						currTime = (schTime-initTime);
						if (TimerService.this.state==RunningState.STOPPED || 
								TimerService.this.state==RunningState.RESETTED) {
							cancel();
							if (TimerService.this.state==RunningState.RESETTED) {
								currTime = 0;
								initTime = 0;
								setCount = 1;
							}
						}
						
						_updateUITimer(currTime, currTime-prevTime, setCount);
					}
				}, 0, TIMER_UPDATE_RATE);
			}
		}
	}
	
	private void _shutdownTimer() {
		synchronized(this) {
			if (timer!=null) {
				timer.cancel();
				timer.purge();
				timer = null;
			}
		}
	}
	
	private void _shutdownDelayTimer() {
		if (delayTimer!=null) {
			delayTimer.cancel();
			delayTimer.purge();
			delayTimer = null;
		}
	}
}
