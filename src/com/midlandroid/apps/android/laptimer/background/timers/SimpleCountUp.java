package com.midlandroid.apps.android.laptimer.background.timers;


import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.midlandroid.apps.android.laptimer.util.ServiceCommand;

/**
 * Simple timer for counting up.  The ceiling of the timer can be set so 
 * it will act just like a down counting timer when finished.
 * @author Jason Del Ponte
 *
 */
public class SimpleCountUp extends TimerMode {
	private static final String LOG_TAG = SimpleCountUp.class.getSimpleName();
	
	private TimerUpdateServiceListener updateService;

	private Messenger messenger;
	private boolean alreadyNotified;
	private long maxTime;
	private boolean useMaxTime;
	private long currTime;
	private long lapTime;
	private int lapCount;
	
	/**
	 * Creates a new instance of this timer with the messenger provided,
	 * and no max time limit.
	 * @param messenger
	 */
	public SimpleCountUp(Messenger messenger) {
		useMaxTime = false;
		this.messenger = messenger;
		this.maxTime = 0;

        currTime = lapTime = lapCount = 0;
		alreadyNotified = false;
	}
	
	
	/**
	 * Creates a new instance of this timer with the messenger provided,
	 * and a max time specified.
	 * @param messenger
	 * @param maxTime
	 */
	public SimpleCountUp(Messenger messenger, int maxTime) {
		useMaxTime = true;
		this.messenger = messenger;
		this.maxTime = maxTime;
		
		currTime = lapTime = lapCount = 0;
		alreadyNotified = false;
	}


	@Override
	public void procTimerUpdate(long updateTime) {
		// Increment the counter
		currTime += updateTime;
		lapTime += updateTime;
		
		// Only do the checks if max time is selected to be used.
		if (useMaxTime && currTime >= maxTime && !alreadyNotified) {
			_notifyMessenger(ServiceCommand.CMD_SOUND_ALARM);
			_notifyMessenger(ServiceCommand.CMD_TIMER_FINISHED);
			alreadyNotified = true;
			return;
		}
		
		// Update the UI
		if (updateService != null) {
			updateService.setCurrentTime(currTime);
			updateService.setLapTime(lapTime);
		}
	}

	
	@Override
	public void procLapEvent() {
		// Internal count of lap timer
		lapCount++;
		
		// Lap increment
		if (updateService != null) {
			updateService.doLapCountIncrement(currTime, lapTime, lapCount);
		}
		
		// Reset the lap time
		lapTime = 0;
	}


	@Override
	public void procRefreshUI() {
		if (updateService != null) {
			updateService.setCurrentTime(currTime);
			updateService.setLapTime(lapTime);
			updateService.setLapCount(lapCount);
		}
	}
	
	@Override
	public void procResetTimer() {
		currTime = lapTime = lapCount = 0;
	}


	@Override
	public String getTimerModeName() {
		return LOG_TAG;
	}
	
	
	/**
	 * Notifies the provided messenger with the command passed in
	 * @param cmd message id to use
	 */
	private void _notifyMessenger(final int cmd) {
		Message msg = Message.obtain();
		msg.what = cmd;
		try {
			messenger.send(msg);
		} catch (RemoteException e) {
			Log.e(LOG_TAG, "Failed to notify messenger", e);
		}
	}

	@Override
	public void setUpdateServiceListener(TimerUpdateServiceListener updateServiceListener) {
		updateService = updateServiceListener;
	}


	@Override
	public TimerUpdateServiceListener getUpdateServiceListener() {
		return updateService;
	}
}
