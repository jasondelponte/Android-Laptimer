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
	private SimpleCountUpData data;
	
	
	/**
	 * Creates a new instance of this timer with the messenger provided,
	 * and no max time limit.
	 * @param messenger
	 */
	public SimpleCountUp(Messenger messenger) {
		this.messenger = messenger;
		
		data = new SimpleCountUpData();
		SimpleCountUpData curData = data;
		

		curData.useMaxTime = false;
		curData.maxTime = 0;

		curData.currTime = curData.lapTime = curData.lapCount = 0;
		curData.alreadyNotified = false;
	}
	
	
	/**
	 * Creates a new instance of this timer with the messenger provided,
	 * and a max time specified.
	 * @param messenger
	 * @param maxTime
	 */
	public SimpleCountUp(Messenger messenger, int maxTime) {
		this.messenger = messenger;
		
		data = new SimpleCountUpData();
		SimpleCountUpData curData = data;
		
		curData.useMaxTime = true;
		curData.maxTime = maxTime;
		
		curData.currTime = curData.lapTime = curData.lapCount = 0;
		curData.alreadyNotified = false;
	}
	
	
	/**
	 * Creates a new instance of this timer with perviously saved data.
	 * @param messenger
	 * @param savedData
	 */
	public SimpleCountUp(Messenger messenger, SimpleCountUpData savedData) {
		this.messenger = messenger;
		
		data = savedData;
	}


	@Override
	public void procTimerUpdate(long updateTime) {
		SimpleCountUpData curData = data;
		
		// Increment the counter
		curData.currTime += updateTime;
		curData.lapTime += updateTime;
		
		// Only do the checks if max time is selected to be used.
		if (curData.useMaxTime && curData.currTime >= curData.maxTime && !curData.alreadyNotified) {
			_notifyMessenger(ServiceCommand.CMD_SOUND_ALARM);
			_notifyMessenger(ServiceCommand.CMD_TIMER_FINISHED);
			curData.alreadyNotified = true;
			return;
		}
		
		// Update the UI
		if (updateService != null) {
			updateService.setCurrentTime(curData.currTime);
			updateService.setLapTime(curData.lapTime);
		}
	}

	
	@Override
	public void procLapEvent() {
		SimpleCountUpData curData = data;
		// Internal count of lap timer
		curData.lapCount++;
		
		// Lap increment
		if (updateService != null) {
			updateService.doLapCountIncrement(curData.currTime, curData.lapTime, curData.lapCount);
		}
		
		// Reset the lap time
		curData.lapTime = 0;
	}


	@Override
	public void procRefreshUI() {
		SimpleCountUpData curData = data;
		
		if (updateService != null) {
			updateService.setCurrentTime(curData.currTime);
			updateService.setLapTime(curData.lapTime);
			updateService.setLapCount(curData.lapCount);
		}
	}
	
	@Override
	public void procResetTimer() {
		SimpleCountUpData curData = data;
		
		curData.currTime = curData.lapTime = curData.lapCount = 0;
	}


	@Override
	public String getTimerModeName() {
		return LOG_TAG;
	}

	/**
	 * Sets the data storage object
	 */
	@Override
	public void setData(TimerModeData modeData) {
		data = (SimpleCountUpData)modeData;
	}
	
	/**
	 * Returns the data storage object
	 */
	@Override
	public TimerModeData getData() {
		return data;
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
	
	/**
	 * Return's the timer's current time.
	 * @return
	 */
	@Override
	public long getCurTime() {
		return data.currTime;
	}
}
