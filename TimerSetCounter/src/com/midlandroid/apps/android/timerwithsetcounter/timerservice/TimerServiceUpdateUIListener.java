package com.midlandroid.apps.android.timerwithsetcounter.timerservice;


public interface TimerServiceUpdateUIListener {
	public void updateTimerUI(final long currTime, final long lapTime, final int setCount);
	public void addLapToUI(final LapData lapData);
	public void clearLapList();
}
