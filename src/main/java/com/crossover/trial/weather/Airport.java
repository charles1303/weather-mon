package com.crossover.trial.weather;

import java.io.Serializable;

public class Airport implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8118546568016293418L;

	public enum DST {
		E("Europe"), A("US/Canada"), S("South America"), O("Australia"), Z("New Zealand"), N("None"), U("Unknown");
		private String value;
 
		private DST(String value) {
			this.value = value;
		}
	}
	private Long id;
	private String city;
	
	private String country = "";
	
	private String icao_code = "";
	
	private double longitude;
	
	private int altitude;
	
	private double timezone;
	
	private DST daylight_saving_time;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getIcao_code() {
		return icao_code;
	}

	public void setIcao_code(String icao_code) {
		this.icao_code = icao_code;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public int getAltitude() {
		return altitude;
	}

	public void setAltitude(int altitude) {
		this.altitude = altitude;
	}

	public double getTimezone() {
		return timezone;
	}

	public void setTimezone(double timezone) {
		this.timezone = timezone;
	}

	public DST getDaylight_saving_time() {
		return daylight_saving_time;
	}

	public void setDaylight_saving_time(DST daylight_saving_time) {
		this.daylight_saving_time = daylight_saving_time;
	}
	
	public static void main(String[] args){
		DST dst = Airport.DST.valueOf("E");
		
		System.out.println("Company Value: " + dst.value + " - Comapny Name: " + dst.name());
	}
	
	
}
