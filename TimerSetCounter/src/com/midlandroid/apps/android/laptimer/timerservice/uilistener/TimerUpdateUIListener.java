package com.midlandroid.apps.android.laptimer.timerservice.uilistener;

import com.midlandroid.apps.android.laptimer.timerservice.LapData;


public interface TimerUpdateUIListener {
	public void updateTimerUI(final long currTime, final long lapTime, final int setCount);
	public void addLapToUI(final LapData lapData);
	public void clearLapList();
}
