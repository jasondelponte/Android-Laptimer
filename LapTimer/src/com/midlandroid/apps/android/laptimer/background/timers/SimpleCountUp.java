package com.midlandroid.apps.android.laptimer.background.timers;

import java.util.List;
import java.util.Vector;

import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.midlandroid.apps.android.laptimer.util.LapData;
import com.midlandroid.apps.android.laptimer.util.MessageId;

/**
 * Simple timer for counting up.  The ceiling of the timer can be set so 
 * it will act just like a down counting timer when finished.
 * @author Jason Del Ponte
 *
 */
public class SimpleCountUp extends TimerMode {
	private static final String LOG_TAG = SimpleCountUp.class.getSimpleName();
	
	private List<LapData> lapDataList;
	private TimerUpdateUIListener updateUI;

	private Messenger messenger;
	private boolean alreadyNotified;
	private long maxTime;
	private boolean useMaxTime;
	private long currTime;
	private long lapTime;
	
	/**
	 * Creates a new instance of this timer with the messenger provided,
	 * and no max time limit.
	 * @param messenger
	 */
	public SimpleCountUp(Messenger messenger) {
		useMaxTime = false;
		this.messenger = messenger;
		this.maxTime = 0;
		
		
		
		currTime = 0;
		alreadyNotified = false;
		lapDataList = new Vector<LapData>();
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
		
		currTime = lapTime = 0;
		alreadyNotified = false;
	}


	@Override
	public void procTimerUpdate(long updateTime) {
		// Increment the counter
		currTime += updateTime;
		lapTime += updateTime;
		
		// Only do the checks if max time is selected to be used.
		if (useMaxTime && currTime >= maxTime && !alreadyNotified) {
			_notifyMessenger(MessageId.CMD_SOUND_ALARM);
			_notifyMessenger(MessageId.CMD_TIMER_FINISHED);
			alreadyNotified = true;
			return;
		}
		
		// Update the UI
		if (updateUI != null) {
			updateUI.updateCurrentTime(currTime);
			updateUI.updateLapTime(lapTime);
		}
	}

	
	@Override
	public void procLapEvent() {
		// Lap increment
		if (updateUI != null) {
			updateUI.updateLapIncrement(currTime, lapTime);
		}
		
		// Reset the lap time
		lapTime = 0;
	}


	@Override
	public void procRefreshUI() {
		if (updateUI != null) {
			updateUI.updateCurrentTime(currTime);
			updateUI.updateLapTime(lapTime);
			updateUI.updateLapList(lapDataList);
		}
	}
	
	@Override
	public void procResetTimer() {
		currTime = lapTime = 0;
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
	public void setUpdateUIListener(TimerUpdateUIListener updateUIListener) {
		updateUI = updateUIListener;
	}


	@Override
	public TimerUpdateUIListener getUpdateUIListener() {
		return updateUI;
	}
}
