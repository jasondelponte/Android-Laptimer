package com.midlandroid.apps.android.laptimer.background;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.midlandroid.apps.android.laptimer.background.timers.TimerMode;
import com.midlandroid.apps.android.laptimer.background.timers.TimerMode.RunningState;
import com.midlandroid.apps.android.laptimer.util.ServiceCommand;
import com.midlandroid.apps.android.laptimer.util.TextUtil;

public final class TimerState implements Serializable {
	private static final long serialVersionUID = 3445902777734654981L;

	private int timerCommand;
    private int timerCommandToRestore;
    private RunningState runningState;
	private boolean wasDelayTimerAlreadyUsed;
	private long timerStartTime;
	private long timerPausedAt;
	private long timerStartOffset;

	private List<String> timerHistory;
	private Stack<TimerMode> timerModes;
	
	/**
	 * Default constructor for the timer state object
	 */
	public TimerState() {
		timerHistory = new ArrayList<String>();
		timerModes = new Stack<TimerMode>();

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
	/**
	 * Returns the timer history 
	 * @return
	 */
	public String getHistoryAsMultiLineString() {
		return TextUtil.stringListToMultiLineString(timerHistory);
	}

	
	/**
	 * Adds a timer mode to the stack of modes
	 * @param mode
	 */
	public void pushToTimerModeStack(TimerMode mode) { timerModes.push(mode); }
	/**
	 * Returns the timer mode currently at the top of the timer mode stack.
	 * The mode will be left on the stack.
	 * @return
	 */
	public TimerMode peekAtTimerModeStack() { return timerModes.peek(); }
	/**
	 * Returns the timer mode currently at the top of the timer mode stack.
	 * The mode will be removed from the top of the stack.
	 * @return
	 */
	public TimerMode popFromTimerModeStack() { return timerModes.pop(); }
	
	
	/**
	 * Sets the running state of the timer.
	 * @param state
	 */
	public void setRunningState(RunningState state) { runningState = state; }
	/**
	 * Returns the current state of the timer.
	 * @return
	 */
	public RunningState getRunningState() { return runningState; }
	
	
	/**
	 * Sets the command to process for the timer.
	 * @param command
	 */
	public void setTimerCommand(int command) { timerCommand = command; }
	/**
	 * Returns the currently set timer command.
	 * @return
	 */
	public int getTimerCommand() { return timerCommand; }
	

	/**
	 * Sets the timer command that will be restored at a later time.
	 * @param command
	 */
	public void setTimerCommandToRestore(int command) { timerCommandToRestore = command; }
	/**
	 * Returns the timer command to be restored at a later time.
	 * @return
	 */
	public int getTimerCommandToRestore() { return timerCommandToRestore; }
	
	
	/**
	 * Sets if the delay timer has already been used.
	 * @param flag
	 */
	public void setWasDelayTimerAlreadyUsed(boolean flag) { wasDelayTimerAlreadyUsed = flag; }
	/**
	 * Returns if the delay timer has already been used.
	 * @return
	 */
	public boolean getWasDelayTimerAlreadyUsed() { return wasDelayTimerAlreadyUsed; }
	

	/**
	 * Sets the Timer's original start time in milliseconds.
	 * @param timerStartTime
	 */
	public void setTimerStartTime(long timerStartTime) { this.timerStartTime = timerStartTime; }
	/**
	 * Returns the time the timer was original set at in milliseconds.
	 * @return
	 */
	public long getTimerStartTime() { return timerStartTime; }
	
	
	/**
	 * Sets the time the timer was paused at in milliseconds.
	 * @param timerPausedAt
	 */
	public void setTimerPausedAt(long timerPausedAt) { this.timerPausedAt = timerPausedAt; }
	/**
	 * Returns the time the timer was paused at in milliseconds.
	 * @return
	 */
	public long getTimerPausedAt() { return timerPausedAt; }


	/**
	 * Sets the timer's start offset time in milliseconds.
	 * @param timerStartOffset
	 */
	public void setTimerStartOffset(long timerStartOffset) { this.timerStartOffset = timerStartOffset; }
	/**
	 * Returns the timer's start offset time in milliseconds.
	 * @return
	 */
	public long getTimerStartOffset() { return timerStartOffset; }
	
	
	/**
	 * Resets the state, and all of its associated values.
	 */
	public void resetState() {
		timerCommand = timerCommandToRestore = ServiceCommand.CMD_DONT_PROC_TIMER_UPDATES;
		runningState = RunningState.RESETTED;
		
		timerStartTime = timerPausedAt = timerStartOffset = 0;
		wasDelayTimerAlreadyUsed = false;
		
		timerHistory.clear();
	}
}
