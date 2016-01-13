package com.crossover.trial.weather;

/**
 * An internal exception marker
 */
public class WeatherException extends Exception {
	
	public WeatherException(){
		super();
	}
	public WeatherException(String exceptionMessage){
		super(exceptionMessage);
	}
}
