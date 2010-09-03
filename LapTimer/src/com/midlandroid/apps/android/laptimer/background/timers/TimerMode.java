package com.midlandroid.apps.android.laptimer.background.timers;


public abstract class TimerMode {
	public  static enum RunningState {
		RUNNING, STOPPED, RESETTED, TIMER_DELAY
	}
	
	public abstract void procTimerUpdate(long updateTime);
	public abstract void procLapEvent();
	public abstract void procResetTimer();
	public abstract void procRefreshUI();
	
	public abstract String getTimerModeName();
	
	public abstract void setUpdateUIListener(TimerUpdateUIListener updateUIListener);
	public abstract TimerUpdateUIListener getUpdateUIListener();
}
