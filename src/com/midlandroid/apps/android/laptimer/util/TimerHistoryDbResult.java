package com.midlandroid.apps.android.laptimer.util;

import java.util.Date;

public class TimerHistoryDbResult {
	private Date startedAt;
	private Date finishedAt;
	private long duration;
	private String history;
	
	public TimerHistoryDbResult() {
		this.startedAt = new Date();
		this.finishedAt = new Date();
		this.duration = 0;
		this.history = "";	
	}
	
	public TimerHistoryDbResult(Date startedAt, Date finishedAt, long duration, String history) {
		this.startedAt = startedAt;
		this.finishedAt = finishedAt;
		this.duration = duration;
		this.history = history;	
	}
	
	
	
	public void setStartedAt(Date startedAt) {
		this.startedAt = startedAt;
	}
	public Date getStartedAt() {
		return startedAt;
	}
	
	
	public void setFinishedAt(Date finishedAt) {
		this.finishedAt = finishedAt;
	}
	public Date getFinishedAt() {
		return finishedAt;
	}
	
	
	public void setDuration(long duration) {
		this.duration = duration;
	}
	public long getDuration() {
		return duration;
	}
	
	
	public void setHistory(String history) {
		this.history = history;
	}
	public String getHistory() {
		return history;
	}
}
