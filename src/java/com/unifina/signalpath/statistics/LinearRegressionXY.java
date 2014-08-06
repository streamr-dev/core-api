package com.unifina.signalpath.statistics;

import java.util.ArrayList;

import org.apache.commons.math3.stat.regression.SimpleRegression;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.IntegerParameter;
import com.unifina.signalpath.TimeSeriesInput;
import com.unifina.signalpath.TimeSeriesOutput;

public class LinearRegressionXY extends AbstractSignalPathModule {

	IntegerParameter windowLength = new IntegerParameter(this,"windowLength",60);
	
	TimeSeriesInput x = new TimeSeriesInput(this,"inX");
	TimeSeriesInput y = new TimeSeriesInput(this,"inY");
	
	TimeSeriesOutput slope = new TimeSeriesOutput(this,"slope");
	TimeSeriesOutput intercept = new TimeSeriesOutput(this,"intercept");
	TimeSeriesOutput error = new TimeSeriesOutput(this,"error");
	TimeSeriesOutput rsq = new TimeSeriesOutput(this,"R^2");
	
	ArrayList<double[]> values;
	SimpleRegression regression = new SimpleRegression();
	
	@Override
	public void init() {
		addInput(windowLength);
		addInput(x);
		addInput(y);
		addOutput(slope);
		addOutput(intercept);
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
		newVal[0] = x.value;
		newVal[1] = y.value;
		
		values.add(newVal);
		regression.addData(newVal[0],newVal[1]);
		
		if (values.size()==windowLength.getValue()) {
			double s = regression.getSlope();
			if (s != Double.NaN) {
				slope.send(regression.getSlope());
				intercept.send(regression.getIntercept());
				error.send(regression.getMeanSquareError());
				rsq.send(regression.getRSquare());
			}
		}
	}
	
	@Override
	public void clearState() {
		values = new ArrayList<double[]>(windowLength.getValue());
		regression = new SimpleRegression();
	}
	
}
