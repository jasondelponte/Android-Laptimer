package com.midlandroid.apps.android.laptimer.background;

import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

import com.midlandroid.apps.android.laptimer.background.timers.SimpleCountDown;
import com.midlandroid.apps.android.laptimer.background.timers.SimpleCountUp;
import com.midlandroid.apps.android.laptimer.background.timers.TimerMode;
import com.midlandroid.apps.android.laptimer.background.timers.TimerMode.RunningState;
import com.midlandroid.apps.android.laptimer.background.timers.TimerUpdateUIListener;
import com.midlandroid.apps.android.laptimer.util.AppPreferences;
import com.midlandroid.apps.android.laptimer.util.ServiceCommand;


import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.util.Log;

public class BackgroundSrvc extends Service {
	private static final String LOG_TAG = BackgroundSrvc.class.getSimpleName();

	private static int TIMER_UPDATE_STEP_MILLS = 100;
	
    // Background service controls
	private TaskQueue bckgrndTasks;
	private AppPreferences appPrefs;
	private PowerManager.WakeLock wakeLock;
	private NotificationManager mNM;
	private TimerUpdateUIListener uiUpdateListener;
	
	// Timer controls
	private Stack<TimerMode> timerModes;
	private int timerCommand;
    private int timerCommandToRestore;
    private RunningState timerState;
	private boolean delayTimerAlreadyUsed;
	private long timerStartTime;
	private long timerPausedAt;
	private long timerStartOffset;
	private String timerHistory;
	
	// Timer Task and its container
	private Timer timer;


    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
    	public BackgroundSrvc getService() {
            return BackgroundSrvc.this;
        }
    }
    

    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(LOG_TAG, "onCreate");

		// Get the handle to the notification service
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		
        // Create the background task queue
		bckgrndTasks = new TaskQueue();
		bckgrndTasks.start();
		
		// Create the app preferences handler
		appPrefs = new AppPreferences(this.getBaseContext());
		
		// Initial timer state
		timerState = RunningState.RESETTED;
		
		// Create the stack that will be used to move between the timer modes
		timerModes = new Stack<TimerMode>();
		
		// Create a queue for commands 
		timerCommand = timerCommandToRestore = ServiceCommand.CMD_NONE;
		
		// Set timer mode
		// TODO replace this with user specified timer modes
		timerModes.push(new SimpleCountUp(myMessenger));

		// Initialize other variables
		delayTimerAlreadyUsed = false;
		
		//
		timerHistory = "";
	}
	
	
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		Log.d(LOG_TAG, "onStart");
	}
	
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		// Make sure to clean up and stop the timer on exit
		_doStopTimer();
		
		// Stop timer and clear it
		if (timer != null)
			timer.cancel();
	}
	
	
    /**
     * Incoming messages from external sources.
     * @author Jason Del Ponte
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case ServiceCommand.CMD_START_STOP_TIMER:
            	if (timerState != RunningState.RUNNING) {
        			_doStartTimer();
            	} else {
            		_doStopTimer();
            	}
            	break;
            	
            case ServiceCommand.CMD_LAP_INCREMENT:
            	// ignore the lap increment if the timers not running.
            	if (timerState == RunningState.RUNNING)
            		_doLapIncrement();
            	break;
            	
            case ServiceCommand.CMD_RESET_TIMER:
            	_doResetTimer();
            	break;
            	
            case ServiceCommand.CMD_REFRESH_MAIN_UI:
            	_doRefreshMainUI();
            	break;
            	
            case ServiceCommand.CMD_TIMER_FINISHED:
            	_currTimerFinished();
            	break;
            	
            case ServiceCommand.CMD_SAVE_TIMER_HISTORY:
            	timerHistory = (String)msg.obj;
            	
	        default:
	            super.handleMessage(msg);
	            break;
	        }
        }
    }
    private final IncomingHandler inHandler = new IncomingHandler();
    
    // Target we publish for clients to send messages to IncomingHandler.
    public final Messenger myMessenger = new Messenger(inHandler);
    
    
    /**
     * Loads the application's default shared preferences into memory
     */
    public void updateAppPreferences() {
    	appPrefs.loadPrefs();
    }


	/**
     * Returns the container object of the application's default shared preferences
     * @return
     */
    public AppPreferences getAppPreferences() {
    	return appPrefs;
    }
    
    
    /**
     * Returns the current timer state
     * @return current running state
     */
	public RunningState getTimerState() {
		return timerState;
	}
	
	
	/**
	 * Returns the name of the last timer mode used
	 * @return Timer Mode name
	 */
	public String getTimerModeName() {
		TimerMode mode = timerModes.peek();
		if (mode != null)
			return mode.getTimerName();
		else
			return "No Timer Selected";
	}
	
	
	/**
	 * Returns the time the timer was last started as.
	 * @return start time of the timer
	 */
	public long getTimerStartTime() {
		return timerStartTime;
	}
	
	
	/**
	 * Sets the Timer update UI listener object that can be used by each
	 * timer mode
	 * @param uiListener
	 */
	public void setUpdateUIListener(final TimerUpdateUIListener uiListener) {
		uiUpdateListener = uiListener;
		
		// Set the timer mode's ui update listeners
		for (TimerMode mode : timerModes) {
			mode.setUpdateUIListener(uiListener);
		}
	}
	
	
	/**
	 * Clears all timer modes' UI update listeners
	 */
	public void clearUpdateUIListener() {
		uiUpdateListener = null;
		
		// Clear the timer mode's ui update listeners
		for (TimerMode mode : timerModes) {
			mode.setUpdateUIListener(null);
		}
	}
	
	
	///////////////////////////////////////////////////
	// Private Methods
	///////////////////////////////////////////////////
	/**
	 * Creates the background timer using the Android 
	 * system message Handler
	 */
	private void _doStartTimer() {
		Log.d(LOG_TAG, "doStartTimer");
		
		// Should a delay timer be used in addition to the normal timer
		if (appPrefs.getUseDelayTimer() && 
				(!delayTimerAlreadyUsed || appPrefs.getUseDelayTimerOnRestarts())) {

			// Create a special count down timer to be used as a delayed timer
			TimerMode mode = new SimpleCountDown(myMessenger, appPrefs.getTimerStartDelay());
			mode.setTimerName("Delay Count Down");
			
			// Add the timer mode to the list
			timerModes.push(mode);
			
			delayTimerAlreadyUsed = true;
		}
		
		// Update the timer mode's UI update listeners
		TimerUpdateUIListener uiListener = uiUpdateListener;
		for (TimerMode mode : timerModes) {
			mode.setUpdateUIListener(uiListener);
		}
		
		// Let the timer process know what we are doing.
		timerCommand = ServiceCommand.CMD_PROC_TIMER_UPDATES;
		
		// Find out what time we are starting the timers at and start them
		if (timerStartTime == 0)
			timerStartTime = System.currentTimeMillis();
		else
			timerStartOffset = System.currentTimeMillis() - timerPausedAt;
		
		// Remove the old call backs if there were any.
		if (timer != null)
			timer.cancel();
		
		// Schedule the the events
		timer = new Timer(false);
		timer.schedule(new TimerTask() {
			private long totalRunTime = 0;
			@Override
			public void run() {
				// Run the timer modes time update process
				TimerMode mode = timerModes.peek();
				if (mode != null) {
					
					// Process the timer commands if there are any
					boolean doTimeUpdate = true;
					boolean doScheduleNextUpdate = true;
					
					switch(timerCommand) {
					case ServiceCommand.CMD_LAP_INCREMENT:
						timerCommand = timerCommandToRestore;
						mode.procLapEvent();
						break;
						
					case ServiceCommand.CMD_REFRESH_MAIN_UI:
						timerCommand = timerCommandToRestore;
						mode.procRefreshUI();
						if (uiUpdateListener!=null) {
							uiUpdateListener.setTimerHistory(timerHistory);
						}
						break;
						
					case ServiceCommand.CMD_PROC_TIMER_UPDATES:
						timerState = RunningState.RUNNING;
						break;
						
					case ServiceCommand.CMD_STOP_TIMER:
						doTimeUpdate = false;
						timerState = RunningState.STOPPED;
						break;
						
					case ServiceCommand.CMD_RESET_TIMER:
						doTimeUpdate = false;
						doScheduleNextUpdate = false;
						mode.procResetTimer();
						
						// Reset the start timer offsets
						timerStartTime = timerPausedAt = totalRunTime = 0; 
						
						// Reset the UI
						if (uiUpdateListener!=null) {
							uiUpdateListener.resetUI();
						}					
		
						// Update the timer state
						timerState = RunningState.RESETTED;
						break;
					}
					
					// Update the timer with the new time.
					if (doTimeUpdate) {
						// get the time difference since last update
						long currSysTime = System.currentTimeMillis();
						
						// Update the total runtime in case we were paused.
						if (timerStartOffset!=0) {
							// the total time should only be offset once per restart
							totalRunTime += timerStartOffset;
							timerStartOffset = 0;
						}
						
						// Calculate the new run time and current slice
						long newRunTime = currSysTime - timerStartTime;
						long currTimeSlice = newRunTime - totalRunTime;
						
						// Do the time slice
						mode.procTimerUpdate(currTimeSlice);
						
						// Save off the total run time
						totalRunTime = newRunTime;
					}
		
					// Stop the scheduled
					if (!doScheduleNextUpdate)
						this.cancel();
		//				inHandler.postDelayed(this, TIMER_UPDATE_STEP_MILLS);
					
				} else {
					// Nothing to do, but stop the timer
					_doStopTimer();
				}
			}
		}, 0, TIMER_UPDATE_STEP_MILLS);

		// Grab the power manager wake lock if it's enabled
		if (appPrefs.getUseWakeLock()) {
			_grabWakeLock();
		}
	}
	
	
	/**
	 * Increment the timers lap
	 */
	private void _doLapIncrement() {
		Log.d(LOG_TAG, "doLapIncrement");
		
		// Tell the timer a new lap event was received
		timerCommandToRestore = timerCommand;
		timerCommand = ServiceCommand.CMD_LAP_INCREMENT;
	}
	
	
	/**
	 * Stops the background timer updates
	 */
	private void _doStopTimer() {
		Log.d(LOG_TAG, "doStopTimer");
		
		// Save off the time the timer was stopped in case it is restarted
		timerPausedAt = System.currentTimeMillis();
		
		// Tell the timer to stop processing updates.
		timerCommand = ServiceCommand.CMD_STOP_TIMER;
		
		// Release the power manager wake lock if it was enabled
		if (appPrefs.getUseWakeLock()) {
			_releaseWakeLock();
		}
	}
	
	
	/**
	 * Reset the timer
	 */
	private void _doResetTimer() {
		Log.d(LOG_TAG, "doResetTimer");
	
		// Tell the timer to reset and stop
		timerCommand = ServiceCommand.CMD_RESET_TIMER;
		
		// Reset the values
		delayTimerAlreadyUsed = false;
		
		// Release the power manager wake lock if it was enabled
		if (appPrefs.getUseWakeLock()) {
			_releaseWakeLock();
		}
	}
	
	
	/**
	 * Refresh the main UI
	 */
	private void _doRefreshMainUI() {
		Log.d(LOG_TAG, "doRefreshMainUI");
		
		// Tell the timer to refresh the UI
		timerCommandToRestore = timerCommand;
		timerCommand = ServiceCommand.CMD_REFRESH_MAIN_UI;
	}
	
    
    /**
     * The current timer has finished and the next timer in the 
     * stack needs to be activated.
     */
    private void _currTimerFinished() {
    	TimerUpdateUIListener uiUpdate = uiUpdateListener;
    	
    	TimerMode finMode = timerModes.pop();
    	TimerMode nexMode = timerModes.peek();
    	// notify the user that the current timer has finished
    	if (uiUpdate != null) {
    		if (finMode != null)
    			uiUpdate.addTextLineToTimerHistory(finMode.getTimerName()+" Finished");
    		
    		if (nexMode != null)
    			uiUpdate.addTextLineToTimerHistory(nexMode.getTimerName()+" Started");
    		
    		// Reset the lap data since they are timer specific
    		uiUpdate.resetLaps();
    	}
    	
    	// Notify the user that the timer has finished
    	if (appPrefs.getUseAudioAlerts())
    		_soundAudioAlert();
	}
    
    
    /**
     * Create an audible alert notifying the user that the current timer has
     * been completed.
     */
    private void _soundAudioAlert() {
    	// TODO create audio alert
    }
	
	
	/**
	 * Grab the wake lock preventing the screen from timing out
	 * if the option is set
	 */
	private void _grabWakeLock() {
		if (wakeLock == null) {
			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, LOG_TAG);
		}
		
		if (!wakeLock.isHeld())
			wakeLock.acquire();
	}
	
	
	/**
	 * Releases the previously grabbed wake lock
	 */
	private void _releaseWakeLock() {
		if (wakeLock != null && wakeLock.isHeld()) {
			wakeLock.release();
		}
	}
}
