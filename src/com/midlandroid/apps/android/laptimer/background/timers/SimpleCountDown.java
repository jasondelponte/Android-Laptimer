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

	private TimerUpdateServiceListener updateService;
	private Messenger messenger;
	private SimpleCountDownData data;
	
	/**
	 * Create a new instance of the class 
	 * @param messenger
	 * @param startFrom
	 */
	public SimpleCountDown(final Messenger messenger, final long startFrom) {
		this.messenger = messenger;

		data = new SimpleCountDownData();
		SimpleCountDownData curData = data;
		
		// Initialize the timer values
		curData.currTime = curData.startTime = startFrom;
		curData.alreadyNotified = false;
	}
	
	
	/**
	 * Creates a new instance of the class with previously saved data.
	 * @param messenger
	 * @param savedData
	 */
	public SimpleCountDown(final Messenger messenger, SimpleCountDownData savedData) {
		this.messenger = messenger;
		
		data = savedData;
	}
	
	
	/**
	 * Called from the timer processing each time step.
	 * @param timeUpdate
	 */
	public void procTimerUpdate(final long timeUpdate) {
		SimpleCountDownData curData = data;
		
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
		SimpleCountDownData curData = data;
		
		curData.currTime = curData.startTime;
	}


	@Override
	public void procRefreshUI() {
		SimpleCountDownData curData = data;
		
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
		data = (SimpleCountDownData)modeData;
	}
	
	/**
	 * Returns the data storage object
	 */
	@Override
	public TimerModeData getData() {
		return data;
	}
}
