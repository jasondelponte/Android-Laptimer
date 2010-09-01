package com.midlandroid.apps.android.laptimer.timerservice.mode;

import com.midlandroid.apps.android.laptimer.timerservice.uilistener.TimerUpdateUIListener;

public abstract class TimerMode {
	public  static enum RunningState {
		RUNNING, STOPPED, RESETTED, TIMER_DELAY
	}
	
	public abstract void startTimer();
	public abstract void stopTimer();
	public abstract void resetTimer();
	public abstract void killTimer();
	public abstract void lapTimer();
	public abstract void refreshUI();
	public abstract String getTimerModeName();
	
	protected RunningState runningState;
	public RunningState getState() {
		return runningState;
	}
	public void setState(RunningState state) {
		runningState = state;
	}
	
	public abstract void setUpdateUIListener(TimerUpdateUIListener updateUIListener);
	public abstract TimerUpdateUIListener getUpdateUIListener();
}
