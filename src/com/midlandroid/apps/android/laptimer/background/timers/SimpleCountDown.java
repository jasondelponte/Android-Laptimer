package com.midlandroid.apps.android.laptimer.background.timers;


import java.io.Serializable;

import com.midlandroid.apps.android.laptimer.background.timers.SimpleCountUp.Data;
import com.midlandroid.apps.android.laptimer.util.ServiceCommand;

import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

/**
 * Simple timer for counting down.
 * @author Jason Del Ponte
 */
public class SimpleCountDown extends TimerMode {
	private static final String LOG_TAG = SimpleCountDown.class.getSimpleName();

	private TimerUpdateServiceListener updateService;
	private Messenger messenger;
	
	public class Data implements TimerModeData, Serializable {
		private static final long serialVersionUID = -7201523372621637071L;
		
		private boolean alreadyNotified;
		private long currTime;
		private long startTime;
	}
	private Data data;
	
	/**
	 * Create a new instance of the class 
	 * @param messenger
	 * @param startFrom
	 */
	public SimpleCountDown(final Messenger messenger, final long startFrom) {
		this.messenger = messenger;
		
		Data curData = data;
		
		// Initialize the timer values
		curData.currTime = curData.startTime = startFrom;
		curData.alreadyNotified = false;
	}
	
	
	/**
	 * Called from the timer processing each time step.
	 * @param timeUpdate
	 */
	public void procTimerUpdate(final long timeUpdate) {
		Data curData = data;
		
		// Decrement the counter
		curData.currTime -= timeUpdate;
		
		// Check if it is time to finish, and don't spam the messenger
		if (curData.currTime <= 0 && !curData.alreadyNotified) {
			_notifyMessenger(ServiceCommand.CMD_SOUND_ALARM);
			_notifyMessenger(ServiceCommand.CMD_TIMER_FINISHED);
			curData.alreadyNotified = true;
		}
		
		// Update the UI
		if (updateService!=null)
			updateService.setCurrentTime(curData.currTime);
	}


	@Override
	public void procLapEvent() {
		// Do nothing
	}


	@Override
	public void procResetTimer() {
		Data curData = data;
		
		curData.currTime = curData.startTime;
	}


	@Override
	public void procRefreshUI() {
		Data curData = data;
		
		if (updateService!=null) {
			updateService.setCurrentTime(curData.currTime);
			updateService.setLapTime(0);
		}
	}
	
	
	/**
	 * Returns this classes timer mode name
	 * @return
	 */
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

	/**
	 * Sets the data storage object
	 */
	@Override
	public void setData(TimerModeData modeData) {
		data = (Data)modeData;
	}
	
	/**
	 * Returns the data storage object
	 */
	@Override
	public TimerModeData getData() {
		return data;
	}
}
