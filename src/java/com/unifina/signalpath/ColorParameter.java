package com.unifina.signalpath;

import com.unifina.utils.Color;

public class ColorParameter extends Parameter<Color> {

	public ColorParameter(AbstractSignalPathModule owner, String name, Color defaultValue) {
		super(owner, name, defaultValue, "Color");
	}

	@Override
	public Color parseValue(String rgb) {
		String[] splitted = rgb.split("\\(|\\, |\\,|\\)");
		int r = Integer.parseInt(splitted[1]);
		int g = Integer.parseInt(splitted[2]);
		int b = Integer.parseInt(splitted[3]);
		return new Color(r, g, b);
	}

	@Override
	public Object formatValue(Color value) {
		return value.toString();
	}
}
