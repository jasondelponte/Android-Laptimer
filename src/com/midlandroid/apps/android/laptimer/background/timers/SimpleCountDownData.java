package com.midlandroid.apps.android.laptimer.background.timers;

import java.io.Serializable;

public class SimpleCountDownData implements TimerModeData, Serializable {
	private static final long serialVersionUID = -7201523372621637071L;
	
	protected boolean alreadyNotified;
	protected long currTime;
	protected long startTime;
}
