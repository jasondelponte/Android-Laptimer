package com.midlandroid.apps.android.laptimer.background.timers;

public interface TimerUpdateUIListener {
	public void setCurrentTime(final long currTime);
	public void setLapTime(final long lapTime);
	public void setLapCount(final int count);
	public void setTimerHistory(final String history);
	public void resetUI();
}
