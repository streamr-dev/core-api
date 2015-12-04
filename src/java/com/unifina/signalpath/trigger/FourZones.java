package com.unifina.signalpath.trigger;

import java.util.ArrayList;
import java.util.Map;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.DoubleParameter;
import com.unifina.signalpath.IntegerParameter;
import com.unifina.signalpath.PossibleValue;
import com.unifina.signalpath.TimeSeriesInput;
import com.unifina.signalpath.TimeSeriesOutput;

public class FourZones extends AbstractSignalPathModule {

	DoubleParameter highTrigger = new DoubleParameter(this,"highTrigger",0.8);
	DoubleParameter highRelease = new DoubleParameter(this,"highRelease",0.2);
	DoubleParameter lowRelease = new DoubleParameter(this,"lowRelease",-0.2);
	DoubleParameter lowTrigger = new DoubleParameter(this,"lowTrigger",-0.8);
	
	IntegerParameter mode = new ModeParameter(this);
	
	TimeSeriesInput input = new TimeSeriesInput(this,"in");
	TimeSeriesOutput out = new TimeSeriesOutput(this,"out");
	
	Integer state = null;
	Integer armed = null;
	
	public FourZones() {
		mode.canToggleDrivingInput = false;
		mode.canBeFeedback = false;
	}
	
	@Override
	public void init() {
		addInput(mode);
		addInput(highTrigger);
		addInput(highRelease);
		addInput(lowRelease);
		addInput(lowTrigger);
		addInput(input);
		addOutput(out);
	}
	
	@Override
	public void sendOutput() {

		if (mode.getValue()==1) {
			if (input.value >= highTrigger.getValue())
				state = 1;
			else if (input.value <= lowTrigger.getValue())
				state = -1;
			else if (input.value <= highRelease.getValue() && (state==null || state==1) || input.value >= lowRelease.getValue() && (state==null || state==-1))
				state = 0;
		}
		else {
			// Over high trigger
			if (input.value >= highTrigger.getValue()) {
				armed = 1;
				if (state==null) state = 0;
			}
			// Under low trigger
			else if (input.value <= lowTrigger.getValue()) {
				armed = -1;
				if (state==null) state = 0;
			}
			// In-between high triggers
			else if (input.value < highTrigger.getValue() && input.value > highRelease.getValue()) {
				if (armed!=null && state!=null && armed==1 && state!=1) {
					armed = 0;
					state = 1;
				}
				if (armed==null) armed = 0;
				if (state==null) state = 0;
			}
			// In-between low triggers
			else if (input.value > lowTrigger.getValue() && input.value < lowRelease.getValue()) {
				if (armed!=null && state!=null && armed==-1 && state!=-1) {
					armed = 0;
					state = -1;
				}
				if (armed==null) armed = 0;
				if (state==null) state = 0;
			}
			// In middle section
//			else {
//				state = 0
//				armed = 0
//			}
			else if (input.value <= highRelease.getValue() && (state==null || armed==null || state==1 || armed==1) || input.value >= lowRelease.getValue() && (state==null || armed==null || state==-1 || armed==-1)) {
				state = 0;
				armed = 0;
			}
		}
		if (state != null)
			out.send(state.doubleValue());
		
	}
	
	@Override
	public void clearState() {
		state = null;
		armed = null;
	}

	private static class ModeParameter extends IntegerParameter {

		public ModeParameter(AbstractSignalPathModule owner) {
			super(owner, "mode", 1);
		}

		@Override
		public Map<String,Object> getConfiguration() {
			Map<String,Object> config = super.getConfiguration();
			ArrayList<PossibleValue> possibleValues = new ArrayList<>();
			possibleValues.add(new PossibleValue("enter",1));
			possibleValues.add(new PossibleValue("exit",2));
			config.put("possibleValues",possibleValues);
			return config;
		}
	}
}
