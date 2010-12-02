package com.midlandroid.apps.android.laptimer.util;


public class TimerHistoryDbRecord {
	private int id;
	private long startedAt;
	private String desc;
	private long finishedAt;
	private long duration;
	private String history;
	
	public TimerHistoryDbRecord() {
		this.setId(0);
		this.startedAt = 0;
		this.finishedAt = 0;
		this.duration = 0;
		this.history = "";	
	}
	
	public TimerHistoryDbRecord(int id, long startedAt, String desc, long finishedAt, long duration, String history) {
		this.id = id;
		this.startedAt = startedAt;
		this.desc = desc;
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

	
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public String getDesc() {
		return desc;
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
