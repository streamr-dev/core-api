package com.unifina.signalpath.input;


import com.unifina.signalpath.ModuleWithUI;
import com.unifina.signalpath.Propagator;
import com.unifina.signalpath.RuntimeRequest;
import com.unifina.signalpath.RuntimeResponse;

import java.util.Map;

public abstract class InputModule extends ModuleWithUI {

	protected boolean uiEventSendPending = false;

	public InputModule() {
		super();
		resendLast = 1;
	}

	@Override
	protected void handleRequest(RuntimeRequest request, RuntimeResponse response) {
		super.handleRequest(request, response);
		if (request.getType().equals("uiEvent")) {
			onInput(request, response);
			setSendPending(true);
			uiEventSendPending = true;
			if (getUiEventPropagator()==null) {
				setUiEventPropagator(new Propagator(this));
				getUiEventPropagator().initialize();
			}
			trySendOutput();
			if (wasReady()) {
				getUiEventPropagator().propagate();
				uiEventSendPending = false;
			}
			response.setSuccess(true);
		}
	}

	@Override
	public Map<String, Object> getConfiguration() {
		Map<String, Object> config = super.getConfiguration();
		config.put("widget", getWidgetName());
		return config;
	}

	protected abstract String getWidgetName();
	protected abstract void onInput(RuntimeRequest request, RuntimeResponse response);
}
