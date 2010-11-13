package com.midlandroid.apps.android.laptimer.background;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.midlandroid.apps.android.laptimer.background.timers.TimerMode;
import com.midlandroid.apps.android.laptimer.util.TextUtil;

public final class TimerState implements Serializable {
	private static final long serialVersionUID = 3445902777734654981L;
	
	
	protected long currTimeCount;
	protected long startTime;
	protected long stateSavedAtTime;
	
	protected List<String> timerHistory;
	protected Stack<TimerMode> timreModeStack;
	
	/**
	 * Default constructor for the timer state object
	 */
	public TimerState() {
		timerHistory = new ArrayList<String>();

		resetState();
	}
	
	
	/**
	 * Adds a new item to the end of the timer history
	 * @param item
	 */
	public void addItemToHistory(String item) {
		timerHistory.add(item);
	}
	
	
	/**
	 * Adds a new item to the front of the timer history
	 * @param item
	 */
	public void addItemToTopOfHistory(String item) {
		timerHistory.add(0, item);
	}
	
	public String getHistoryAsMultiLineString() {
		return TextUtil.stringListToMultiLineString(timerHistory);
	}
	
	
	/**
	 * Resets the state, and all of its associated values.
	 */
	public void resetState() {
		currTimeCount = startTime = stateSavedAtTime = 0;
		timerHistory.clear();
	}
}
