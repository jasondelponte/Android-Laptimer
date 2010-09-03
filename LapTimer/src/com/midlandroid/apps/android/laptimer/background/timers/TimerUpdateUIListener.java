package com.midlandroid.apps.android.laptimer.background.timers;

import java.util.List;

import com.midlandroid.apps.android.laptimer.util.LapData;


public interface TimerUpdateUIListener {
	public void updateCurrentTime(final long currTime);
	public void updateLapTime(final long lapTime);
	public void updateLapIncrement(final long currTime, final long lapTime);
	public void updateLapList(final List<LapData> laps);
}
