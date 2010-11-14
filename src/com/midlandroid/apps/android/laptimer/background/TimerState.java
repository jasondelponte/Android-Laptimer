package com.midlandroid.apps.android.laptimer.background;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import android.content.Context;
import android.os.Messenger;

import com.midlandroid.apps.android.laptimer.background.timers.SimpleCountDown;
import com.midlandroid.apps.android.laptimer.background.timers.SimpleCountDownData;
import com.midlandroid.apps.android.laptimer.background.timers.SimpleCountUp;
import com.midlandroid.apps.android.laptimer.background.timers.SimpleCountUpData;
import com.midlandroid.apps.android.laptimer.background.timers.TimerMode;
import com.midlandroid.apps.android.laptimer.background.timers.TimerMode.RunningState;
import com.midlandroid.apps.android.laptimer.background.timers.TimerModeData;
import com.midlandroid.apps.android.laptimer.background.timers.TimerUpdateServiceListener;
import com.midlandroid.apps.android.laptimer.util.ServiceCommand;
import com.midlandroid.apps.android.laptimer.util.TextUtil;

public final class TimerState implements Serializable {
	private static final long serialVersionUID = 3445902777734654981L;

	// State flags
	private int timerCommand;
    private int timerCommandToRestore;
    private RunningState runningState;
	private boolean wasDelayTimerAlreadyUsed;
	
    // Real world time in milliseconds
	private long timerStartTime;
	private long timerPausedAt;
	// System time in milliseconds
	private long timerStartOffset;
	
	private List<String> timerHistory;
	
	// The modes them selves do not need to be stored only their data
	transient private Stack<TimerMode> timerModes;
	
	// Stack of mode data to be stored
	private Stack<TimerModeData> timerModesData;
	
	// State time saved/restored at
	private long timeStateSavaedAt;
	private long timeStateRestoredAt;
	private boolean wasSaved;
	
	
	/**
	 * Default constructor for the timer state object
	 */
	public TimerState() {
		timerHistory = new ArrayList<String>();
		timerModes = new Stack<TimerMode>();
		timerModesData = new Stack<TimerModeData>();
		wasSaved = false;

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
	 * Adds a timer mode data object to the stack.
	 * @param data
	 */
	public void pushToTimerModeDataStack(TimerModeData data) { timerModesData.push(data); }
	/**
	 * Returns the timer mode data currently at the top of the mode stack.
	 * The data will be left on the stack.
	 * @return
	 */
	public TimerModeData peekAtTimerModeDataStack() { return timerModesData.peek(); }
	/**
	 * Returns the timer mode data current at the top of the mode stack.
	 * the data will be removed from the stack.
	 * @return
	 */
	public TimerModeData popFromTimeModeStack() {return timerModesData.pop(); }
	
	
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
	public void setTimerStartedAt(long timerStartTime) { this.timerStartTime = timerStartTime; }
	/**
	 * Returns the time the timer was original set at in milliseconds.
	 * @return
	 */
	public long getTimerStartedAt() { return timerStartTime; }
	
	
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
	 * Sets the real world time the timer state was saved to storage.
	 * @param timeStateSavaedAt
	 */
	public void setTimeStateSavaedAt(long timeStateSavaedAt) { this.timeStateSavaedAt = timeStateSavaedAt; }
	/**
	 * Returns the real world time the timer state was saved to storage.
	 * @return
	 */
	public long getTimeStateSavaedAt() { return timeStateSavaedAt; }
	

	/**
	 * Sets the real world time the timer state was restored from storage.
	 * @param timeStateRestoredAt
	 */
	public void setTimeStateRestoredAt(long timeStateRestoredAt) { this.timeStateRestoredAt = timeStateRestoredAt; }
	/**
	 * Returns the real world time the timer state was restored from storage.
	 * @return
	 */
	public long getTimeStateRestoredAt() { return timeStateRestoredAt; }
	
	
	/**
	 * Resets the state, and all of its associated values.
	 */
	public void resetState() {
		timerCommand = timerCommandToRestore = ServiceCommand.CMD_DONT_PROC_TIMER_UPDATES;
		runningState = RunningState.RESETTED;
		
		timerStartTime = timerPausedAt = timerStartOffset = timeStateSavaedAt = timeStateRestoredAt = 0;
		wasSaved = wasDelayTimerAlreadyUsed = false;
		
		timerHistory.clear();
	}
	
	
	/**
	 * Sets if the previous timer state was saved.
	 * @param flag
	 */
	public void setWasSaved(boolean flag) { wasSaved = flag; }
	/**
	 * Returns whether or not the timer state was previously saved.
	 * @return
	 */
	public boolean getWasSaved() { return wasSaved; }


	/**
	 * Saves off the timer mode's data to an internal 
	 * stack of data objects.
	 */
	public void saveTimerModesData() {
		for (TimerMode mode : timerModes) {
			timerModesData.push(mode.getData());
		}
	}


	/**
	 * Rebuilds the timer mode stack using the
	 * data values previously stored.
	 * @param messenger
	 * @param serviceListener 
	 */
	public void restoreTimerModesFromData(Messenger messenger, TimerUpdateServiceListener serviceListener) {
		// Make sure the timer modes are valid before restoring them
		if (timerModes == null)
			timerModes = new Stack<TimerMode>();
		else
			timerModes.clear();
		
		// Looping over the modes restore the timers
		for (TimerModeData data : timerModesData) {
			if (data instanceof SimpleCountUpData) {
				TimerMode mode = new SimpleCountUp(messenger, (SimpleCountUpData)data);
				mode.setUpdateServiceListener(serviceListener);
				timerModes.push(mode);
			}
			
			else if (data instanceof SimpleCountDownData) {
				TimerMode mode = new SimpleCountDown(messenger, (SimpleCountDownData)data);
				mode.setUpdateServiceListener(serviceListener);
				timerModes.push(mode);
			}
		}
	}
}
