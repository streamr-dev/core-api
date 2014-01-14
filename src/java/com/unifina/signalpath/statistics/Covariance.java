package com.unifina.signalpath.statistics;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.IntegerParameter;
import com.unifina.signalpath.TimeSeriesInput;
import com.unifina.signalpath.TimeSeriesOutput;
import com.unifina.utils.SlidingDoubleArray;

public class Covariance extends AbstractSignalPathModule {

	IntegerParameter windowLength = new IntegerParameter(this,"windowLength",60);
	
	TimeSeriesInput x = new TimeSeriesInput(this,"inX");
	TimeSeriesInput y = new TimeSeriesInput(this,"inY");
	
	TimeSeriesOutput out = new TimeSeriesOutput(this,"cov");
	
	SlidingDoubleArray xValues;
	SlidingDoubleArray yValues;
	
	org.apache.commons.math3.stat.correlation.Covariance cov = new org.apache.commons.math3.stat.correlation.Covariance();
	

	@Override
	public void init() {
		addInput(windowLength);
		addInput(x);
		addInput(y);
		addOutput(out);
	}
	
	@Override
	public void sendOutput() {
		if (xValues==null) {
			xValues = new SlidingDoubleArray(windowLength.getValue());
			yValues = new SlidingDoubleArray(windowLength.getValue());
		}
		else if (xValues.maxSize() != windowLength.getValue()) {
			xValues.changeSize(windowLength.getValue());
			yValues.changeSize(windowLength.getValue());
		}
		
		xValues.add(x.value);
		yValues.add(y.value);
		
		if (xValues.isFull()) {
			double covariance = cov.covariance(xValues.getValues(), yValues.getValues());
			out.send(covariance);
		}
		
	}
	
	@Override
	public void clearState() {
		xValues = new SlidingDoubleArray(windowLength.getValue());
		yValues = new SlidingDoubleArray(windowLength.getValue());
	}


}
