package com.midlandroid.apps.android.laptimer.background;

import java.io.Serializable;
import java.util.List;
import java.util.Stack;

import com.midlandroid.apps.android.laptimer.background.timers.TimerMode;

public final class TimerState implements Serializable {
	private static final long serialVersionUID = 3445902777734654981L;
	
	
	protected long currTimeCount;
	protected long startTime;
	protected long stateSavedAtTime;
	
	protected List<String> timerHistory;
	protected Stack<TimerMode> timreModeStack;
}
