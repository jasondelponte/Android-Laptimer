package com.midlandroid.apps.android.laptimer.util;

import com.midlandroid.apps.android.laptimer.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Class that provides a simple interface to get current application's
 * user specified preferences.
 * @author Jason Del Ponte
 */
public class AppPreferences {
	private static final String LOG_TAG = AppPreferences.class.getSimpleName();
	
	private Context parentContext;
	
	// Preferences
	private long timerStartDelay;
	private boolean useDelayTimer;
	private boolean useDelayTimerOnRestarts;
	private boolean useWakeLock;
	private boolean useAudioAlerts;
	private boolean useOneClickTextCopy;
	private String outfileRootPath;
	private boolean useSecondChanceReset;
	
	/**
	 * Creates a new instance of the application preference accessors.
	 * @param context Parent context required to access application's preferences.
	 */
	public AppPreferences(Context context) {
		parentContext = context;
	}
	
	/**
	 * Loads the application preferences from storage into memory.
	 */
	public void loadPrefs() {
		Context context = parentContext;

		// Debug starting to access application preferences
		Log.d(LOG_TAG, "Begining to read default shared preferences.");
		
		// Access the default shared preferences
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		Resources res = context.getResources();
				
		// Delay timer amount
		try {
			timerStartDelay = Integer.valueOf(prefs.getString(
					res.getString(R.string.pref_timer_start_delay_key), "0"))*1000;
		} catch (NumberFormatException e) {
			timerStartDelay = 0;
		}		
		// Should the delay timer be used?
		useDelayTimer = (timerStartDelay > 0);
				
		// Use delay timer on restarts?
		useDelayTimerOnRestarts = prefs.getBoolean(
				res.getString(R.string.pref_timer_start_delay_on_restarts_key), false);
		
		// Should the power manager wake lock be used?
		useWakeLock = prefs.getBoolean(
				res.getString(R.string.pref_use_wakelock_key), true);
		
		// Should Audio alerts be played?
		useAudioAlerts = false;
		
		// Use delay timer on restarts?
		useOneClickTextCopy = prefs.getBoolean(
				res.getString(R.string.pref_one_click_txt_cpy_key), true);
		
		// Output path for the lap timer data file to be written to 
		outfileRootPath=""; //prefs.getString(res.getString(R.string.pref_outfile_root_key), "/sdcard/");
		
		// Should the user be given a second chance when the reset menu item is selected.
		useSecondChanceReset = prefs.getBoolean(res.getString(R.string.pref_second_chance_reset_key), true);
		
		// Debug write out the known values.
		Log.d(LOG_TAG, "timerStartDelay: "+Long.toString(timerStartDelay) + 
				", useDelayTimer: "+Boolean.toString(useDelayTimer)+
				", useDelayTimerOnRestarts: "+Boolean.toString(useDelayTimerOnRestarts)+
				", useWakeLock: "+Boolean.toString(useWakeLock)+
				", useAudioAlerts: "+Boolean.toString(useAudioAlerts)+
				", useOneClickTextCopy: "+Boolean.toString(useOneClickTextCopy)+
				", outfileRootPath: "+outfileRootPath+
				", useSecondChanceReset: "+useSecondChanceReset);
	}
	
	public long getTimerStartDelay() {return timerStartDelay;}
	public boolean getUseDelayTimer() {return useDelayTimer;}
	public boolean getUseDelayTimerOnRestarts() {return useDelayTimerOnRestarts;}
	public boolean getUseWakeLock() {return useWakeLock;}
	public boolean getUseAudioAlerts() {return useAudioAlerts;}
	public boolean getUseOneClickTextCopy() {return useOneClickTextCopy;}
	public String getOutfileRootPath() {return outfileRootPath;}
	public boolean getUseSecondChanceReset() {return useSecondChanceReset;}
}
