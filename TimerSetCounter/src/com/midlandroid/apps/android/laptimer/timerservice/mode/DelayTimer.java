package com.midlandroid.apps.android.laptimer.timerservice.mode;

import java.util.Timer;
import java.util.TimerTask;

import android.os.Handler;
import android.os.Message;

import com.midlandroid.apps.android.laptimer.timerservice.uilistener.DelayTimerUpdateUIListener;
import com.midlandroid.apps.android.laptimer.util.MessageId;

public class DelayTimer {
	private static final int TIMER_UPDATE_RATE = 100;
	
	private DelayTimerUpdateUIListener uiListener;
	private Handler notifyTo;
	private Timer timer;
	private TimerTask timerTask;

	//////////////////////////////////////
	// Create new object
	//////////////////////////////////////
	public DelayTimer(Handler notifyTo) {
		this.notifyTo = notifyTo;
		timer = new Timer();
		timerTask = null;
	}

	//////////////////////////////////////
	// Public Controls
	//////////////////////////////////////
	public void startTimer(final long startTime) {
		// Setup the timer task
		if (timerTask!=null) {
			timerTask.cancel();
			timerTask = null;
		}
		
		// If not enough time was added don't bother
		// with the timer.
		if (startTime < 1000) {
			_notifyServiceOfCompletion();
			return;
		}
		
		// Create the timer thread.
		timerTask = new TimerTask() {
			private long delayInitTime = 0;
			private long time;
			
			@Override
			public void run() {
				long schTime = this.scheduledExecutionTime();			
				if (delayInitTime==0)
					delayInitTime = schTime;
				
				time = startTime - (schTime-delayInitTime);
				_updateUI(time+1000);
				
				// Cancel the timer if when the time runs out
				if (time <= 0) {
					_notifyServiceOfCompletion();
					cancel();
				}
			}
		};
		
		timer.schedule(timerTask, 0, TIMER_UPDATE_RATE);
	}

	public void stopTimer() {
		if (timerTask!=null)
			timerTask.cancel();
	}
	
	public void killTimer() {
		timer.cancel();
		timer.purge();
		timer = null;
	}
	
	public DelayTimerUpdateUIListener getUpdateUIListener() {
		return uiListener;
	}

	public void setUpdateUIListener(DelayTimerUpdateUIListener updateUIListener) {
		uiListener = updateUIListener;
	}

	//////////////////////////////////////
	// Private Methods
	//////////////////////////////////////
	private void _updateUI(final long time) {
		if (uiListener!=null)
			uiListener.updateDelayTimerUI(time);
	}
	
	private void _notifyServiceOfCompletion() {
		if (notifyTo!=null) {
			Message msg = Message.obtain();
			msg.arg1 = MessageId.SRC_DELAYTIMER;
			msg.arg2 = MessageId.DelayTimerCmd.CMD_TIMER_FINISHED;
			notifyTo.sendMessage(msg);
		}
	}
}
