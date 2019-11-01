package com.unifina.signalpath.utils;

import com.unifina.datasource.ITimeListener;
import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.IntegerParameter;
import com.unifina.signalpath.TimeSeriesInput;
import com.unifina.signalpath.TimeSeriesOutput;

import java.io.Serializable;
import java.util.Date;

public class Barify extends AbstractSignalPathModule implements ITimeListener {
	
	IntegerParameter barLength = new IntegerParameter(this,"barLength",60);
	
	TimeSeriesInput input = new TimeSeriesInput(this,"in");
	
	TimeSeriesOutput open = new TimeSeriesOutput(this,"open");
	TimeSeriesOutput high = new TimeSeriesOutput(this,"high");
	TimeSeriesOutput low = new TimeSeriesOutput(this,"low");
	TimeSeriesOutput close = new TimeSeriesOutput(this,"close");
	TimeSeriesOutput avg = new TimeSeriesOutput(this,"avg");
	TimeSeriesOutput sum = new TimeSeriesOutput(this,"sum");
	
	Bar currentBar = new Bar(null,null,null,null,null,0,0);
	Bar previousBar = null;
	
	@Override
	public void sendOutput() {
		double value = input.value;
		
		if (currentBar.start != null) {
			if (value > currentBar.high)
				currentBar.high = value;
			if (value < currentBar.low)
				currentBar.low = value;
				
			currentBar.sum += value;
			currentBar.count++;
		} else {
			currentBar = new Bar(getGlobals().time, input.value, input.value, input.value, input.value, value, 1);
		}
	}
	
	@Override
	public void init() {
		setPropagationSink(true);
		
		addInput(barLength);
		
		addInput(input);
		
		open.setNoRepeat(false);
		addOutput(open);
		high.setNoRepeat(false);
		addOutput(high);
		low.setNoRepeat(false);
		addOutput(low);
		close.setNoRepeat(false);
		addOutput(close);
		avg.setNoRepeat(false);
		addOutput(avg);
		sum.setNoRepeat(false);
		addOutput(sum);
	}
	
	@Override
	public void clearState() {
		resetBar();
	}
	
	public void resetBar() {
		currentBar = new Bar(null, null, null, null, null, 0, 0);
		previousBar = null;
	}
	
	@Override
	public void setTime(Date time) {
		// First bar
		if (currentBar.start==null) {
			if (input.value!=null)
				currentBar = new Bar(time, input.value, input.value, input.value, input.value, 0, 0);
		}
		// Bar ready, propagate
		else  {
			currentBar.close = input.value;

			open.send(currentBar.open);
			high.send(currentBar.high);
			low.send(currentBar.low);
			close.send(currentBar.close);
			sum.send(currentBar.sum);
			avg.send(currentBar.count>0 ? currentBar.sum/currentBar.count : currentBar.close);

			previousBar = currentBar;
			currentBar = new Bar(time, previousBar.close, previousBar.close, previousBar.close, previousBar.close, 0, 0);
		}
	}

	@Override
	public int tickRateInSec() {
		return barLength.getValue();
	}

	static class Bar implements Serializable {
		public Date start;
		public Double open;
		public Double high;
		public Double low;
		public Double close;
		public double sum;
		public int count;
		
		public Bar(Date start, Double open, Double high, Double low,
				Double close, double sum, int count) {
			this.start = start;
			this.open = open;
			this.high = high;
			this.low = low;
			this.close = close;
			this.sum = sum;
			this.count = count;
		}
	}

}
