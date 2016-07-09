package com.namhto.weather;

import java.sql.Timestamp;

public class InstantWeather {

	private int temp = 0;
	private String sky = "";
	private Timestamp date;
	
	public InstantWeather(int temp, String sky, Timestamp t) {
		this.temp = temp;
		this.sky = sky;
		this.date = t;
	}
	
	public int getTemp() {
		return temp;
	}
	public void setTemp(int temp) {
		this.temp = temp;
	}
	public String getSky() {
		return sky;
	}
	public void setSky(String sky) {
		this.sky = sky;
	}
	public Timestamp getDate() {
		return date;
	}
	public void setDate(Timestamp date) {
		this.date = date;
	}
}