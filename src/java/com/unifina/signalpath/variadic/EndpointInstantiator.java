package com.unifina.signalpath.variadic;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.Endpoint;

interface EndpointInstantiator<E extends Endpoint> {
	E instantiate(AbstractSignalPathModule module, String endpointName);
}
