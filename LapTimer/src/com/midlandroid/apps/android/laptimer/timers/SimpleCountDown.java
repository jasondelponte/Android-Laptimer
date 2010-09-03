package com.midlandroid.apps.android.laptimer.timers;

import com.midlandroid.apps.android.laptimer.util.MessageId;

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
	
	private Messenger messenger;
	private boolean alreadyNotified;
	private long currTime;
	
	/**
	 * Create a new instance of the class 
	 * @param messenger
	 * @param startFrom
	 */
	public SimpleCountDown(final Messenger messenger, final long startFrom) {
		this.messenger = messenger;
		
		// Initialize the timer values
		currTime = startFrom;
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
			_notifyMessenger(MessageId.CMD_SOUND_ALARM);
			_notifyMessenger(MessageId.CMD_TIMER_FINISHED);
			alreadyNotified = true;
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
	public void setUpdateUIListener(TimerUpdateUIListener updateUIListener) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public TimerUpdateUIListener getUpdateUIListener() {
		// TODO Auto-generated method stub
		return null;
	}
}
