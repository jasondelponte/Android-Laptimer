package com.midlandroid.apps.android.laptimer.background.timers;

import java.io.Serializable;

public class SimpleCountUpData implements TimerModeData, Serializable {
	private static final long serialVersionUID = -92500719128719502L;
	
	protected boolean alreadyNotified;
	protected long maxTime;
	protected boolean useMaxTime;
	protected long currTime;
	protected long lapTime;
	protected int lapCount;
}
