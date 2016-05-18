package com.unifina.signalpath.variadic;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.Input;
import com.unifina.signalpath.TimeSeriesInput;

import java.io.Serializable;

/**
 * Used by <code>VariadicInput</code> to instantiate inputs of certain type with pre-determined settings.
 */
public interface InputInstantiator<T> extends EndpointInstantiator<Input<T>> {

	class TimeSeries implements InputInstantiator<Double>, Serializable {
		@Override
		public Input<Double> instantiate(AbstractSignalPathModule owner, String inputName) {
			return new TimeSeriesInput(owner, inputName);
		}
	}

	class SimpleObject implements InputInstantiator<Object>, Serializable {
		@Override
		public Input<Object> instantiate(AbstractSignalPathModule owner, String inputName) {
			return new Input<>(owner, inputName, "Object");
		}
	}
}
