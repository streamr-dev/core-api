package com.unifina.signalpath.color;

import com.unifina.math.MovingAverage;
import com.unifina.signalpath.*;
import com.unifina.utils.Color;

import java.util.List;
import java.util.Map;

public class Gradient extends AbstractSignalPathModule {
	
	DoubleParameter min = new DoubleParameter(this, "minValue", 0d);
	DoubleParameter max = new DoubleParameter(this, "maxValue", 1d);

	TimeSeriesInput in = new TimeSeriesInput(this, "in");

	ColorParameter minColor = new ColorParameter(this, "minColor", new Color(255, 255, 255));
	ColorParameter maxColor = new ColorParameter(this, "maxColor", new Color(0, 0, 0));

	Output<Color> out = new Output<>(this, "color", "Color");

	@Override
	public void init() {
		addInput(min);
		addInput(max);
		addInput(minColor);
		addInput(maxColor);
		addInput(in);
		addOutput(out);
	}

	@Override
	public void sendOutput() {
		if (in.getValue() <= min.getValue()) {
			out.send(minColor.getValue());
		} else if (in.getValue() >= max.getValue()) {
			out.send(maxColor.getValue());
		} else {
			Double ratio = in.getValue() / (min.getValue() + max.getValue());
			int r = (int)(minColor.getValue().getRed() + (maxColor.getValue().getRed() - minColor.getValue().getRed()) * ratio);
			int g = (int)(minColor.getValue().getGreen() + (maxColor.getValue().getGreen() - minColor.getValue().getGreen()) * ratio);
			int b = (int)(minColor.getValue().getBlue() + (maxColor.getValue().getBlue() - minColor.getValue().getBlue()) * ratio);
			out.send(new Color(r, g, b));
		}
	}
	
	@Override
	public void clearState() {}
}
