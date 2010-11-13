package com.midlandroid.apps.android.laptimer.background.timers;

public interface TimerUpdateServiceListener {
	public void setCurrentTime(final long currTime);
	public void setLapTime(final long lapTime);
	public void setLapCount(final int count);
	public void doLapCountIncrement(final long currTime, final long lapTime, final int lapCount);
	public void setTimerHistory(final String history);
	public void resetUI();
}
