package com.crossover.trial.weather;

import java.io.Serializable;

/**
 * A collected point, including some information about the range of collected values
 *
 * @author code test administrator
 */

public class DataPoint implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = 617572264827949966L;

	private double mean = 0.0;

    private int first = 0;

    private int median = 0;

    private int last = 0;

    private int count = 0;

    
    public DataPoint() { }

    
    public double getMean() {
		return mean;
	}

	public void setMean(double mean) {
		this.mean = mean;
	}

	public int getFirst() {
		return first;
	}

	public void setFirst(int first) {
		this.first = first;
	}

	public int getMedian() {
		return median;
	}

	public void setMedian(int median) {
		this.median = median;
	}

	public int getLast() {
		return last;
	}

	public void setLast(int last) {
		this.last = last;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

}
