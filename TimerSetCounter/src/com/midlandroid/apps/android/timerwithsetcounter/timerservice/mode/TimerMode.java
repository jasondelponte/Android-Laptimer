package com.midlandroid.apps.android.timerwithsetcounter.timerservice.mode;

import com.midlandroid.apps.android.timerwithsetcounter.timerservice.uilistener.TimerUpdateUIListener;

public abstract class TimerMode {
	public  static enum RunningState {
		RUNNING, STOPPED, RESETTED, TIMER_DELAY
	}
	
	public abstract void startTimer();
	public abstract void stopTimer();
	public abstract void resetTimer();
	public abstract void lapTimer();
	public abstract void refreshUI();
	
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
