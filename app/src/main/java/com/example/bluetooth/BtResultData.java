package com.example.bluetooth;

import android.util.Log;

public class BtResultData {
	private String id;
	private String timeStamp;
	private String result;
	private String rssi = "-";
	private String duration = "-";
	public int status = Log.VERBOSE;
	
	public BtResultData() {
		super();
	}

	public BtResultData(String id, String timeStamp, String result, String rssi,
	        String duration ) {
		super();
		this.id = id;
		this.timeStamp = timeStamp;
		this.result = result;
		this.rssi = rssi;
		this.duration = duration;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getRssi() {
		return rssi;
	}

	public void setRssi(String rssi) {
		this.rssi = rssi;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}
}
