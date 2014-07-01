package com.unifina.signalpath.statistics;

import java.util.ArrayList;

import org.apache.commons.math3.stat.regression.SimpleRegression;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.IntegerParameter;
import com.unifina.signalpath.TimeSeriesInput;
import com.unifina.signalpath.TimeSeriesOutput;

public class LinearRegression extends AbstractSignalPathModule {

	IntegerParameter windowLength = new IntegerParameter(this,"windowLength",60);
	
	TimeSeriesInput input = new TimeSeriesInput(this,"in");
	
	TimeSeriesOutput slope = new TimeSeriesOutput(this,"slope");
	TimeSeriesOutput error = new TimeSeriesOutput(this,"error");
	TimeSeriesOutput rsq = new TimeSeriesOutput(this,"R^2");
	
	ArrayList<double[]> values;
	SimpleRegression regression = new SimpleRegression();
	int counter = 0;
	
	@Override
	public void init() {
		addInput(windowLength);
		addInput(input);
		addOutput(slope);
		addOutput(error);
		addOutput(rsq);
	}
	
	@Override
	public void sendOutput() {
		if (values==null)
			values = new ArrayList<double[]>(windowLength.getValue());
		
		while (values.size()>=windowLength.getValue()) {
			double[] removed = values.remove(0);
			regression.removeData(removed[0],removed[1]);
		}
		
		double[] newVal = new double[2];
		newVal[0] = (double)counter++;
		newVal[1] = input.value;
		
		values.add(newVal);
		regression.addData(newVal[0],newVal[1]);
		
		if (values.size()==windowLength.getValue()) {
			slope.send(regression.getSlope());
			error.send(regression.getMeanSquareError());
			rsq.send(regression.getRSquare());
		}
	}
	
	@Override
	public void clearState() {
		values = new ArrayList<double[]>(windowLength.getValue());
		regression = new SimpleRegression();
		counter = 0;
	}
	
}
