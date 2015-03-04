package com.unifina.signalpath.charts;

import java.util.LinkedHashMap;

import com.unifina.signalpath.ModuleWithUI;
import com.unifina.signalpath.TimeSeriesInput;

public class Heatmap extends ModuleWithUI {

	TimeSeriesInput latitude = new TimeSeriesInput(this, "latitude");
	TimeSeriesInput longitude = new TimeSeriesInput(this, "longitude");
	TimeSeriesInput value = new TimeSeriesInput(this, "value");

	@Override
	public void init() {
		super.init();
		this.canClearState = false;
		latitude.setDrivingInput(true);
		latitude.canToggleDrivingInput = false;
		latitude.canHaveInitialValue = false;
		latitude.canBeFeedback = false;
		longitude.setDrivingInput(true);
		longitude.canToggleDrivingInput = false;
		longitude.canHaveInitialValue = false;
		longitude.canBeFeedback = false;
		value.setDrivingInput(true);
		value.canToggleDrivingInput = false;
		value.canHaveInitialValue = false;
		value.canBeFeedback = false;
	}
	
	@Override
	public void sendOutput() {
		if (globals.getUiChannel()!=null) {
			globals.getUiChannel().push(new HeatPoint(latitude.getValue(), longitude.getValue(), value.getValue()), uiChannelId);
		}
	}

	@Override
	public void clearState() {

	}

	class HeatPoint extends LinkedHashMap<String,Object> {
		public HeatPoint(Double latitude, Double longitude, Double value) {
			super();
			put("t", "p");
			put("l", latitude);
			put("g", longitude);
			put("v", value);
		}
	}

}
