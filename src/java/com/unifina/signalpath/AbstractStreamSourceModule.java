package com.unifina.signalpath;

import com.unifina.data.IStreamRequirement;
import com.unifina.domain.data.Stream;

public abstract class AbstractStreamSourceModule extends AbstractSignalPathModule implements IStreamRequirement {

	protected StreamParameter streamParameter;
	
	protected String getStreamParameterName() {
		return "stream";
	}
	
	@Override
	public void init() {
		streamParameter = new StreamParameter(this, getStreamParameterName());
		streamParameter.setCheckModuleId(true);
		streamParameter.setUpdateOnChange(true);
		streamParameter.drivingInput = false;
		streamParameter.canToggleDrivingInput = false;
		addInput(streamParameter);
	}
	
	@Override
	public Stream getStream() {
		return streamParameter.getValue();
	}

}
