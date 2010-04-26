package com.midlandroid.apps.android.timerwithsetcounter;

public class LapData {
	private int lapNum;
	private long lapTime;
	private long totalTime;
	
	public LapData() {
		this(0,0,0);
	}
	
	public LapData(int lapNum, long lapTime, long totalTime) {
		setLapNum(lapNum);
		setLapTime(lapTime);
		setTotalTime(totalTime);
	}
	
	public void setLapNum(int lapNum) {this.lapNum=lapNum;}
	public void setLapTime(long lapTime) {this.lapTime=lapTime;}
	public void setTotalTime(long totalTime) {this.totalTime=totalTime;}
	
	public int getLapNum() {return this.lapNum;}
	public long getLapTime() {return this.lapTime;}
	public long getTotalTime() {return this.totalTime;}
}