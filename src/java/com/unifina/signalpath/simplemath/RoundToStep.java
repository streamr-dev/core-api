package com.unifina.signalpath.simplemath;

import com.unifina.signalpath.*;
import com.unifina.utils.DU;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RoundToStep extends AbstractSignalPathModule {

	public static final int MODE_UP = 1;
	public static final int MODE_DOWN = 2;
	public static final int MODE_TOWARDS_ZERO = 3;
	public static final int MODE_AWAYFROM_ZERO = 4;
	
	IntegerParameter mode = new ModeParameter(this, "mode", 1);
	
	DoubleParameter step = new DoubleParameter(this,"precision",0.01);
	TimeSeriesInput input = new TimeSeriesInput(this,"in");
	TimeSeriesOutput out = new TimeSeriesOutput(this,"out");
	
	@Override
	public void init() {
		addInput(step);
		addInput(mode);
		addInput(input);
		addOutput(out);
	}
	
	@Override
	public void sendOutput() {
		int m = mode.getValue();
		switch(m) {
		
		case MODE_UP:
			out.send(DU.roundTo(input.value, step.getValue(), false));
			break;
		case MODE_DOWN:
			out.send(DU.roundTo(input.value, step.getValue(), true));
			break;			
		case MODE_TOWARDS_ZERO:
			out.send(DU.roundTo(input.value, step.getValue(), input.value>=0));
			break;
		case MODE_AWAYFROM_ZERO:
			out.send(DU.roundTo(input.value, step.getValue(), input.value<0));
			break;			
		}
	}

	@Override
	public void clearState() {

	}

	private static class ModeParameter extends IntegerParameter {

		public ModeParameter(AbstractSignalPathModule owner, String name, Integer defaultValue) {
			super(owner, name, defaultValue);
		}

		@Override
		public Map<String,Object> getConfiguration() {
			Map<String,Object> config = super.getConfiguration();

			ArrayList<Map<String,Object>> possibleValues = new ArrayList<>();

			HashMap<String,Object> up = new HashMap<>();
			up.put("name", "up");
			up.put("value", MODE_UP);
			possibleValues.add(up);

			HashMap<String,Object> down = new HashMap<>();
			down.put("name", "down");
			down.put("value", MODE_DOWN);
			possibleValues.add(down);

			HashMap<String,Object> toZero = new HashMap<>();
			toZero.put("name", "towards zero");
			toZero.put("value", MODE_TOWARDS_ZERO);
			possibleValues.add(toZero);

			HashMap<String,Object> awayZero = new HashMap<>();
			awayZero.put("name", "away from zero");
			awayZero.put("value", MODE_AWAYFROM_ZERO);
			possibleValues.add(awayZero);

			config.put("possibleValues", possibleValues);
			return config;
		}
	}
}
