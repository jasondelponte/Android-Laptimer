package com.midlandroid.apps.android.laptimer.background;

import java.util.ArrayList;
import java.util.List;


import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.SystemClock;
import android.util.Log;

public class BackgroundSrvc extends Service {
	private static final String LOG_TAG = BackgroundSrvc.class.getSimpleName();
	
    /** Keeps track of all current registered clients. */
    private ArrayList<Messenger> mClients = new ArrayList<Messenger>();
    
    public static final int MSG_TIMER_START = 1;
    public static final int MSG_TIMER_STOP = 2;	
	
	
	private TaskQueue bckgrndTasks;
	
	
	// For showing and hiding our notifications
	private NotificationManager mNM;


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
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		
		bckgrndTasks = new TaskQueue();
		bckgrndTasks.start();
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		doStopTimer();
	}
    
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_TIMER_START:
            	doCreateTimer();
            	break;
            case MSG_TIMER_STOP:
            	doStopTimer();
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
	

	private long timerStartTime;
	/**
	 * Runnable used to update the timer
	 */
	private Runnable timerUpdateTask = new Runnable() {
		
		@Override
		public void run() {
			long millis = SystemClock.uptimeMillis() - timerStartTime;
			
			int seconds = (int)(millis/1000);
			int minutes = seconds / 60;
			seconds = seconds % 60;

			// TODO Perform processing on active timer
			
			inHandler.postDelayed(this, 200);
		}
	};
	
	/**
	 * Creates the background timer using the Android 
	 * system message Handler
	 */
	private void doCreateTimer() {
		timerStartTime = System.currentTimeMillis();
		
		inHandler.removeCallbacks(timerUpdateTask);
		inHandler.postDelayed(timerUpdateTask, 200);
		Log.d(LOG_TAG, "doCreateTimer");
	}
	
	/**
	 * Stops the background timer updates
	 */
	private void doStopTimer() {
		inHandler.removeCallbacks(timerUpdateTask);
		Log.d(LOG_TAG, "doStopTimer");
	}
}
