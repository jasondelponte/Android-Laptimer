package com.midlandroid.apps.android.laptimer.background.timers;


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

	//private TimerUpdateUIListener updateUI;
	private TimerUpdateServiceListener updateService;
	
	private Messenger messenger;
	private boolean alreadyNotified;
	private long currTime;
	private long startTime;
	
	/**
	 * Create a new instance of the class 
	 * @param messenger
	 * @param startFrom
	 */
	public SimpleCountDown(final Messenger messenger, final long startFrom) {
		this.messenger = messenger;
		
		// Initialize the timer values
		currTime = startTime = startFrom;
		alreadyNotified = false;
	}
	
	
	/**
	 * Called from the timer processing each time step.
	 * @param timeUpdate
	 */
	public void procTimerUpdate(final long timeUpdate) {
		// Decrement the counter
		currTime -= timeUpdate;
		
		// Check if it is time to finish, and don't spam the messenger
		if (currTime <= 0 && !alreadyNotified) {
			_notifyMessenger(ServiceCommand.CMD_SOUND_ALARM);
			_notifyMessenger(ServiceCommand.CMD_TIMER_FINISHED);
			alreadyNotified = true;
		}
		
		// Update the UI
		if (updateService!=null)
			updateService.setCurrentTime(currTime);
	}


	@Override
	public void procLapEvent() {
		// Do nothing
	}


	@Override
	public void procResetTimer() {
		currTime = startTime;
	}


	@Override
	public void procRefreshUI() {
		if (updateService!=null) {
			updateService.setCurrentTime(currTime);
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
}
