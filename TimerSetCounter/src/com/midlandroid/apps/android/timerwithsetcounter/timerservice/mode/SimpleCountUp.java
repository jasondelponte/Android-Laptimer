package com.midlandroid.apps.android.timerwithsetcounter.timerservice.mode;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.util.Log;

import com.midlandroid.apps.android.timerwithsetcounter.timerservice.LapData;
import com.midlandroid.apps.android.timerwithsetcounter.timerservice.uilistener.TimerUpdateUIListener;

public class SimpleCountUp extends TimerMode {
	private static final int TIMER_UPDATE_RATE = 100;
	
	private TimerUpdateUIListener uiListener;
	private Timer timer;
	private TimerTask timerTask;
	
	private ArrayList<LapData> lapDataList;
	private long currTime;
	private long initTime;
	private long stoppedAtTime;
	private long prevTime;
	private int lapCount;
	
	public SimpleCountUp() {
		timer = new Timer();
		lapDataList = new ArrayList<LapData>();
		
		_init();
	}
	

	//////////////////////////////////////////
	// Overridden public methods
	//////////////////////////////////////////
	@Override
	public void startTimer() {
		setState(RunningState.RUNNING);
		
		// Setup the timer task
		if (timerTask!=null)
			timerTask.cancel();
		
		timerTask = new TimerTask() {
			@Override
			public void run() {
				long schTime = this.scheduledExecutionTime();
				
				if (initTime==0)
					initTime = schTime;
				
				if (stoppedAtTime!=0) {
					initTime += (schTime - (initTime+stoppedAtTime));						
					stoppedAtTime = 0;
				}

				currTime = (schTime-initTime);
				if (getState()!=RunningState.RUNNING) {
					stoppedAtTime = currTime;
					
					if (getState()==RunningState.RESETTED) {
						_init();
					}
					
					_updateUITimer(currTime, currTime-prevTime, lapCount);
					cancel();
				} else {
					_updateUITimer(currTime, currTime-prevTime, lapCount);
				}

			}
		};
		
		timer.schedule(timerTask, 0, TIMER_UPDATE_RATE);
	}

	@Override
	public void stopTimer() {		
		if (getState()==RunningState.RUNNING)
			setState(RunningState.STOPPED);
	}
	
	@Override
	public void killTimer() {
		timer.cancel();
		timer.purge();
		timer = null;
	}

	@Override
	public void resetTimer() {
		setState(RunningState.RESETTED);
		stopTimer();
		
		_init();
		_updateUITimer(currTime, prevTime, lapCount);
		_updateUIClearLaps();
	}

	@Override
	public void lapTimer() {
		if (getState()==RunningState.RUNNING) {
			final LapData lapData = new LapData(lapCount,
					currTime-prevTime, currTime);
			lapDataList.add(lapData);
			
			_updateUIAddLap(lapData);
	
			lapCount++;
			prevTime = currTime;
		}
	}

	@Override
	public void refreshUI() {
		_updateUITimer(currTime, currTime-prevTime, lapCount);
		
		for (LapData lapData : lapDataList) {
			_updateUIAddLap(lapData);
		}
	}
	
	
	//////////////////////////////////////////
	// Private Methods
	//////////////////////////////////////////
	private void _init() {
		setState(RunningState.RESETTED);
		currTime = 0;
		initTime = 0;
		stoppedAtTime = 0;
		prevTime = 0;
		lapCount = 1;
		lapDataList.clear();
	}
	
	private void _updateUITimer(final long currTime, final long lapTime, final int lapCount) {
		if (uiListener!=null) {
			uiListener.updateTimerUI(currTime, lapTime, lapCount);
		}
	}
	
	private void _updateUIAddLap(final LapData lapData) {
		if (uiListener!=null) {
			uiListener.addLapToUI(lapData);
		}
	}
	
	private void _updateUIClearLaps() {
		if (uiListener!=null)
			uiListener.clearLapList();
	}
	

	///////////////////////////////////////////
	// UI Listeners
	///////////////////////////////////////////
	@Override
	public TimerUpdateUIListener getUpdateUIListener() {
		return uiListener;
	}

	@Override
	public void setUpdateUIListener(TimerUpdateUIListener updateUIListener) {
		uiListener = updateUIListener;
	}
}
