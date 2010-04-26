package com.midlandroid.apps.android.timerwithsetcounter;

public final class MessageId {
	public static final int SRC_MAIN = 0;
	public final class MainCmd {
		public static final int CMD_START_STOP_TIMER = 0;
		public static final int CMD_RESET_TIMER = 1;
		public static final int CMD_SET_INCREMENT = 2;
		public static final int CMD_REFRESH = 3;
		public static final int CMD_PREFERENCES_CHANGED = 4;
	}
	
	public static final int SRC_TIMERSERVICE = 1;
	public final class TimerServiceCmd {
		public static final int CMD_SHOW_TIMER_DELAY_UI = 0;
		public static final int CMD_FINISH_TIMER_DELAY_UI = 1;
		public static final int CMD_START_STOP_TIMER = 2;
	}
	
	public static final int SRC_DELAYTIMECOUNTDOWN = 2;
	public final class DelayTimerCountDownCmd {
		public static final int CMD_STOP_TIMER = 0;
	}
}
