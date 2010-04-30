package com.midlandroid.apps.android.timerwithsetcounter.timerservice.mode;

import com.midlandroid.apps.android.timerwithsetcounter.timerservice.uilistener.TimerUpdateUIListener;

public abstract class TimerMode {
	public abstract void startTimer();
	public abstract void stopTimer();
	
	public abstract void setUpdateUIListener(TimerUpdateUIListener updateUIListener);
	public abstract TimerUpdateUIListener getUpdateUIListener();
}
