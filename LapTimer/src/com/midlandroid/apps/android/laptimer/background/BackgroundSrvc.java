package com.midlandroid.apps.android.laptimer.background;

import java.util.Stack;

import com.midlandroid.apps.android.laptimer.timers.SimpleCountDown;
import com.midlandroid.apps.android.laptimer.timers.SimpleCountUp;
import com.midlandroid.apps.android.laptimer.timers.TimerMode;
import com.midlandroid.apps.android.laptimer.timers.TimerMode.RunningState;
import com.midlandroid.apps.android.laptimer.util.AppPreferences;
import com.midlandroid.apps.android.laptimer.util.MessageId;


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
import android.os.SystemClock;
import android.util.Log;

public class BackgroundSrvc extends Service {
	private static final String LOG_TAG = BackgroundSrvc.class.getSimpleName();
	
    ///** Keeps track of all current registered clients. */
    //private ArrayList<Messenger> mClients = new ArrayList<Messenger>();
	
    // Background service controls
	private TaskQueue bckgrndTasks;
	private AppPreferences appPrefs;
	private PowerManager.WakeLock wakeLock;
	private NotificationManager mNM;
	
	// Timer controls
	private Stack<TimerMode> timerModes;
    private RunningState timerState;
	private boolean delayTimerAlreadyUsed;
	private long timerStartTime;


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
		
		// Get the handle to the notification service
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		
        // Create the background task queue
		bckgrndTasks = new TaskQueue();
		bckgrndTasks.start();
		
		// Create the app preferences handler
		appPrefs = new AppPreferences(this.getBaseContext());
		
		// Create the stack that will be used to move between the timer modes
		timerModes = new Stack<TimerMode>();
		
		// Set timer mode
		// TODO replace this with user specified timer modes
		timerModes.push(new SimpleCountUp(myMessenger));

		// Initialize other variables
		delayTimerAlreadyUsed = false;
	}
	
	
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
	}
	
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		// Make sure to clean up and stop the timer on exit
		_doStopTimer();
	}
	
	
    /**
     * Incoming messages from external sources.
     * @author Jason Del Ponte
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MessageId.CMD_START_STOP_TIMER:
            	if (timerState != RunningState.RUNNING) {
        			_doStartTimer();
            	} else {
            		_doStopTimer();
            	}
            	break;
            	
            case MessageId.CMD_LAP_INCREMENT:
            	_doLapIncrement();
            	break;
            	
            case MessageId.CMD_RESET_TIMER:
            	_doResetTimer();
            	break;
            	
            case MessageId.CMD_REFRESH_MAIN_UI:
            	_doRefreshMainUI();
            	break;
            	
            case MessageId.CMD_TIMER_FINISHED:
            	break;
            	
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
			return mode.getTimerModeName();
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
	
	
	///////////////////////////////////////////////////
	// Private Methods
	///////////////////////////////////////////////////
	// Runnable used to update the timer
	private Runnable timerUpdateTask = new Runnable() {
		@Override
		public void run() {
			// get the time difference since last update
			long millis = SystemClock.uptimeMillis() - timerStartTime;

			// Run the timer modes time update process
			TimerMode mode = timerModes.peek();
			if (mode != null) {
				mode.procTimerUpdate(millis);
				
				// Schedule the next update
				inHandler.postDelayed(this, 200);
				
			} else {
				// Nothing to do, but stop the timer
				_doStopTimer();
			}
		}
	};
	
	
	/**
	 * Creates the background timer using the Android 
	 * system message Handler
	 */
	private void _doStartTimer() {
		Log.d(LOG_TAG, "doCreateTimer");
		
		// Should a delay timer be used in addition to the normal timer
		if (appPrefs.getUseDelayTimer() && 
				(!delayTimerAlreadyUsed || appPrefs.getUseDelayTimerOnRestarts())) {
			// Create a special count down timer to be used as a delayed timer
			timerModes.push(new SimpleCountDown(myMessenger, appPrefs.getTimerStartDelay()));
		}
		
		// Find out what time we are starting the timers at and start them
		timerStartTime = System.currentTimeMillis();
		inHandler.removeCallbacks(timerUpdateTask);
		inHandler.postDelayed(timerUpdateTask, 200);

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
		
	}
	
	
	/**
	 * Stops the background timer updates
	 */
	private void _doStopTimer() {
		Log.d(LOG_TAG, "doStopTimer");
		
		inHandler.removeCallbacks(timerUpdateTask);
		
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
	
		// Stop the timer if it's running
		_doStopTimer();
		
		// Reset the values
		delayTimerAlreadyUsed = false;
	}
	
	
	/**
	 * Refresh the main UI
	 */
	private void _doRefreshMainUI() {
		Log.d(LOG_TAG, "doRefreshMainUI");
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
