package com.unifina.signalpath.trigger;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.DoubleParameter;
import com.unifina.signalpath.TimeSeriesInput;
import com.unifina.signalpath.TimeSeriesOutput;

public class PeakDetect extends AbstractSignalPathModule {

	DoubleParameter highZone = new DoubleParameter(this,"highZone",0.8);
	DoubleParameter lowZone = new DoubleParameter(this,"lowZone",-0.8);
	DoubleParameter threshold = new DoubleParameter(this,"threshold",0.0);
	
	TimeSeriesInput input = new TimeSeriesInput(this,"in");
	TimeSeriesOutput out = new TimeSeriesOutput(this,"out");
	
	boolean strictMode = true;
	
	Double prevChange = null;
	Double prevValue = null;
	
	Double prevDerivative = null;
	
	@Override
	public void init() {
		addInput(highZone);
		addInput(lowZone);
		addInput(threshold);
		addInput(input);
		addOutput(out);
	}
	
	@Override
	public void clearState() {
		prevChange = null;
		prevValue = null;
		prevDerivative = null;
	}
	
	@Override
	public void sendOutput() {
		if (prevValue==null)
			prevValue = input.value;

		double currentChange = input.value - prevValue;

		if (prevChange==null)
			prevChange = (strictMode ? currentChange : 0);

		// Has the sign of the derivative changed?
		Double derivative = null;
		if (prevChange < threshold.getValue() && currentChange > threshold.getValue() && (prevDerivative==null || prevDerivative==-1))
			derivative = 1.0;
		// Groovy seems to give 0 > -0 == true if bound to variables, watch out!
		else if (prevChange > (threshold.getValue()==0 ? 0 : -threshold.getValue()) && currentChange < (threshold.getValue()==0 ? 0 : -threshold.getValue()) && (prevDerivative==null || prevDerivative==1))
			derivative = -1.0;

		// Propagate a value every time we have a turn
		if (derivative != null) {
			if (prevValue < lowZone.getValue() && derivative==1)
				out.send(1D);
			else if (prevValue > highZone.getValue() && derivative==-1)
				out.send(-1D);
			else out.send(0D);
		}
		// Else just repeat the previous value
		else if (out.getValue() != null)
			out.send(out.getValue());

		prevValue = input.value;

		// We're interested in the sign change of the change, so skip zero values
		if (currentChange != 0)
			prevChange = currentChange;

		// If the derivative has a value, record it
		if (derivative != null && (prevDerivative == null || !derivative.equals(prevDerivative)))
			prevDerivative = derivative;
	}
	
}
