package com.unifina.signalpath;

import com.unifina.utils.StreamrColor;

public class ColorParameter extends Parameter<StreamrColor> {

	public ColorParameter(AbstractSignalPathModule owner, String name, StreamrColor defaultValue) {
		super(owner, name, defaultValue, "Color");
	}

	@Override
	public StreamrColor parseValue(String rgb) {
		return StreamrColor.parseRGBString(rgb);
	}

	@Override
	public Object formatValue(StreamrColor value) {
		return value.toString();
	}
}
