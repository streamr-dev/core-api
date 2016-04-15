package com.unifina.signalpath.color;


import com.unifina.signalpath.AbstractConstantModule;
import com.unifina.signalpath.ColorParameter;
import com.unifina.signalpath.Output;
import com.unifina.signalpath.Parameter;
import com.unifina.utils.StreamrColor;

public class ColorConstant extends AbstractConstantModule<StreamrColor> {

	@Override
	protected Parameter<StreamrColor> createConstantParameter() {
		return new ColorParameter(this, "value", new StreamrColor(0,0,0));
	}

	@Override
	protected Output<StreamrColor> createOutput() {
		return new Output<>(this, "color", "Color");
	}
}
