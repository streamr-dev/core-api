package com.unifina.signalpath;

import com.unifina.utils.StreamrColor;

public class ColorParameter extends Parameter<StreamrColor> {

	public ColorParameter(AbstractSignalPathModule owner, String name, StreamrColor defaultValue) {
		super(owner, name, defaultValue, "Color");
	}

	@Override
	public StreamrColor parseValue(String rgb) {
		String[] splitted = rgb.split("rgba?\\(|\\, |\\,|\\)");
		int r = Integer.parseInt(splitted[1]);
		int g = Integer.parseInt(splitted[2]);
		int b = Integer.parseInt(splitted[3]);
		double a = 1.0;
		if (splitted.length > 4) {
			a = Double.parseDouble(splitted[4]);
		}
		return new StreamrColor(r, g, b, a);
	}

	@Override
	public Object formatValue(StreamrColor value) {
		return value.toString();
	}
}
