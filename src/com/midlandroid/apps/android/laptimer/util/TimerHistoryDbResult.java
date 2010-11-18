package com.midlandroid.apps.android.laptimer.util;


public class TimerHistoryDbResult {
	private int id;
	private long startedAt;
	private long finishedAt;
	private long duration;
	private String history;
	
	public TimerHistoryDbResult() {
		this.setId(0);
		this.startedAt = 0;
		this.finishedAt = 0;
		this.duration = 0;
		this.history = "";	
	}
	
	public TimerHistoryDbResult(int id, long startedAt, long finishedAt, long duration, String history) {
		this.id = id;
		this.startedAt = startedAt;
		this.finishedAt = finishedAt;
		this.duration = duration;
		this.history = history;	
	}
	

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}
	
	
	public void setStartedAt(long startedAt) {
		this.startedAt = startedAt;
	}
	public long getStartedAt() {
		return startedAt;
	}
	
	
	public void setFinishedAt(long finishedAt) {
		this.finishedAt = finishedAt;
	}
	public long getFinishedAt() {
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
