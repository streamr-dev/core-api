package com.unifina.signalpath.simplemath;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.DoubleParameter;
import com.unifina.signalpath.TimeSeriesInput;
import com.unifina.signalpath.TimeSeriesOutput;

public class LinearMapper extends AbstractSignalPathModule {

	DoubleParameter xMin = new DoubleParameter(this,"xMin",0D);
	DoubleParameter xMax = new DoubleParameter(this,"xMax",1D);
	DoubleParameter yMin = new DoubleParameter(this,"yMin",0D);
	DoubleParameter yMax = new DoubleParameter(this,"yMax",100D);

	TimeSeriesInput input = new TimeSeriesInput(this,"in");
	TimeSeriesOutput output = new TimeSeriesOutput(this,"out");
	
	@Override
	public void init() {
		addInput(xMin);
		addInput(xMax);
		addInput(yMin);
		addInput(yMax);
		addInput(input);
		addOutput(output);
	}

	@Override
	public void sendOutput() {
		double ymx = yMax.getValue();
		double ymn = yMin.getValue();
		double xmx = xMax.getValue();
		double xmn = xMin.getValue();
		double S = (ymx - ymn)/(xmx - xmn);
		double D = ymx - S * xmx;
		double val = input.value * S + D;
		
		if (val > Math.max(ymx,ymn))
			output.send(Math.max(ymx,ymn));
		else if (val < Math.min(ymx,ymn))
			output.send(Math.min(ymx,ymn));
		else output.send(val);
	}

	@Override
	public void clearState() {}

}
