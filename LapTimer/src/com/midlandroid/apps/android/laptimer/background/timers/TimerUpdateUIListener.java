package com.midlandroid.apps.android.laptimer.background.timers;

public interface TimerUpdateUIListener {
	public void updateCurrentTime(final long currTime);
	public void updateLapTime(final long lapTime);
	public void updateLapIncrement(final long currTime, final long lapTime);
	public void updateLapCount(final int count);
	public void resetLaps();
	public void resetUI();
	public void addTextLineToTimerHistory(final String text);
	public void setTimerHistory(final String history);
}
