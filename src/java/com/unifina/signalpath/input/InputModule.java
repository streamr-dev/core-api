package com.unifina.signalpath.input;


import com.unifina.signalpath.ModuleWithUI;
import com.unifina.signalpath.Propagator;
import com.unifina.signalpath.RuntimeRequest;
import com.unifina.signalpath.RuntimeResponse;

public abstract class InputModule extends ModuleWithUI {
	@Override
	protected void handleRequest(RuntimeRequest request, RuntimeResponse response) {
		super.handleRequest(request, response);
		if (request.getType().equals("uiEvent")) {
			onInput(request, response);
			setSendPending(true);
			if (uiEventPropagator==null) {
				uiEventPropagator = new Propagator();
				uiEventPropagator.addModule(this);
			}
			trySendOutput();
			if (wasReady()) {
				uiEventPropagator.propagate();
			}
			response.setSuccess(true);
		}
	}

	protected abstract void onInput(RuntimeRequest request, RuntimeResponse response);
}
