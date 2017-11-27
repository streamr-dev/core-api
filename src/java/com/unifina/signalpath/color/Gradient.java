package com.unifina.signalpath.color;

import com.unifina.signalpath.*;
import com.unifina.utils.StreamrColor;

import java.awt.Color;

public class Gradient extends AbstractSignalPathModule {
	
	DoubleParameter minValue = new DoubleParameter(this, "minValue", 0d);
	DoubleParameter maxValue = new DoubleParameter(this, "maxValue", 1d);

	TimeSeriesInput in = new TimeSeriesInput(this, "in");

	ColorParameter minColor = new ColorParameter(this, "minColor", new StreamrColor(0, 255, 0));
	ColorParameter maxColor = new ColorParameter(this, "maxColor", new StreamrColor(255, 0, 0));

	Output<StreamrColor> out = new Output<>(this, "color", "Color");

	private float[] minHSB;
	private float[] maxHSB;

	@Override
	public void init() {
		addInputs(minValue, maxValue, minColor, maxColor, in);
		addOutput(out);
	}

	@Override
	public void sendOutput() {
		double yMax = 1d;
		double yMin = 0d;
		double xMax = maxValue.getValue();
		double xMin = minValue.getValue();
		double S = (yMax - yMin)/(xMax - xMin);
		double D = yMax - S * xMax;
		float ratio = (float)(in.value * S + D);
		ratio = (float) Math.min(Math.max(ratio, 0d), 1d);

		minHSB = Color.RGBtoHSB(minColor.getValue().getRed(), minColor.getValue().getGreen(), minColor.getValue().getBlue(), null);
		maxHSB = Color.RGBtoHSB(maxColor.getValue().getRed(), maxColor.getValue().getGreen(), maxColor.getValue().getBlue(), null);

		float minH = minHSB[0];
		float maxH = maxHSB[0];
		float newH = calculateHue(minH, maxH, ratio);

		float minS = minHSB[1];
		float maxS = maxHSB[1];
		float newS = calculateBetweenValues(minS, maxS, ratio);

		float minB = minHSB[2];
		float maxB = maxHSB[2];
		float newB = calculateBetweenValues(minB, maxB, ratio);

		double minA = minColor.getValue().getAlpha();
		double maxA = maxColor.getValue().getAlpha();
		double newA = calculateBetweenValues(minA, maxA, ratio);

		out.send(new StreamrColor(new Color(Color.HSBtoRGB(newH, newS, newB)), newA));
	}
	
	@Override
	public void clearState() {}

	private float calculateHue(float min, float max, float ratio) {
		if (max - min > 0.5f) {
			return ((min + 1.0f) * (1.0f - ratio) + max * ratio) % 1.0f;
		} else if (min - max > 0.5f) {
			return (min * (1.0f - ratio) + (max + 1.0f) * ratio) % 1.0f;
		} else {
			return calculateBetweenValues(min, max, ratio);
		}
	}

	private float calculateBetweenValues(float min, float max, float ratio) {
		return min * (1.0f - ratio) + max * ratio;
	}

	private double calculateBetweenValues(double min, double max, double ratio) {
		return (double) calculateBetweenValues((float) min, (float) max, (float) ratio);
	}
}
