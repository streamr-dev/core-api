package com.unifina.signalpath.color;


import com.unifina.signalpath.*;
import com.unifina.utils.StreamrColor;

public class ColorConstant extends AbstractSignalPathModule {
	
	ColorParameter par = new ColorParameter(this, "value", new StreamrColor(0,0,0));

	Output<StreamrColor> out = new Output<>(this,"color", "Color");

	@Override
	public void initialize() {
		for (Input i : out.getTargets())
			i.receive(par.getValue());
	}

	@Override
	public void sendOutput() {
		out.send(par.getValue());
	}

	@Override
	public void clearState() {}
}
