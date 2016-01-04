package com.unifina.signalpath.utils;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.IntegerParameter;
import com.unifina.signalpath.TimeSeriesInput;
import com.unifina.signalpath.TimeSeriesOutput;

import java.io.Serializable;

public class FlexBarify extends AbstractSignalPathModule {
	
	IntegerParameter barLength = new IntegerParameter(this,"barLength",5000);
	
	TimeSeriesInput valueLength = new TimeSeriesInput(this,"valueLength");
	TimeSeriesInput input = new TimeSeriesInput(this,"value");
	
	TimeSeriesOutput open = new TimeSeriesOutput(this,"open");
	TimeSeriesOutput high = new TimeSeriesOutput(this,"high");
	TimeSeriesOutput low = new TimeSeriesOutput(this,"low");
	TimeSeriesOutput close = new TimeSeriesOutput(this,"close");
	TimeSeriesOutput avg = new TimeSeriesOutput(this,"avg");
	TimeSeriesOutput sum = new TimeSeriesOutput(this,"sum");
	
	Bar currentBar = new Bar(null,null,null,null,null,null,0,0);
	Bar previousBar = null;
	
	@Override
	public void sendOutput() {
		
		updateCurrentBar(valueLength.value, input.value);
		
		if (currentBar.start != null) {
			if (input.value > currentBar.high)
				currentBar.high = input.value;
			if (input.value < currentBar.low)
				currentBar.low = input.value;
				
			currentBar.sum += valueLength.value * input.value;
			currentBar.count += valueLength.value;
		}
	}
	
	@Override
	public void init() {
		addInput(barLength);
		
		addInput(valueLength);
		addInput(input);
		input.setDrivingInput(false);
		
		open.noRepeat = false;
		addOutput(open);
		high.noRepeat = false;
		addOutput(high);
		low.noRepeat = false;
		addOutput(low);
		close.noRepeat = false;
		addOutput(close);
		avg.noRepeat = false;
		addOutput(avg);
		sum.noRepeat = false;
		addOutput(sum);
	}
	
	@Override
	public void clearState() {
		resetBar();
	}
	
	public void resetBar() {
		currentBar = new Bar(null, null, null, null, null, null, 0, 0);
		previousBar = null;
	}
	
	private void updateCurrentBar(Double quantity, Double value) {
		// First bar
		if (currentBar.start==null) {
			if (value!=null)
				currentBar = new Bar(0D, quantity, value, value, value, value, 0D, 0);
		}
		else  {
			currentBar.current += quantity;
			// Bar ready
			if (currentBar.current >= currentBar.start + barLength.value) {
				currentBar.close = value;

				open.send(currentBar.open);
				high.send(currentBar.high);
				low.send(currentBar.low);
				close.send(currentBar.close);
				sum.send(currentBar.sum);
				avg.send(currentBar.count>0 ? currentBar.sum/currentBar.count : currentBar.close);

				previousBar = currentBar;
				
				currentBar = new Bar(previousBar.start + barLength.getValue(), previousBar.current, previousBar.close, previousBar.close, previousBar.close, previousBar.close, 0D, 0);
			}
		}
	}

	public static class Bar implements Serializable {
		public Double start;
		public Double current;
		public Double open;
		public Double high;
		public Double low;
		public Double close;
		public double sum;
		public int count;
		
		public Bar(Double start, Double current, Double open, Double high, Double low,
				Double close, double sum, int count) {
			this.start = start;
			this.current = current;
			this.open = open;
			this.high = high;
			this.low = low;
			this.close = close;
			this.sum = sum;
			this.count = count;
		}
	}

}
