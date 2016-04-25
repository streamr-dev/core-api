package com.unifina.signalpath.variadic;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.Input;
import com.unifina.signalpath.TimeSeriesInput;

import java.io.Serializable;

/**
 * Used by <code>VariadicInput</code> to instantiate inputs of certain type with pre-determined settings.
 */
public interface InputInstantiator<T> {

	Input<T> instantiate(AbstractSignalPathModule owner, int index);

	class TimeSeries implements InputInstantiator<Double>, Serializable {
		@Override
		public Input<Double> instantiate(AbstractSignalPathModule owner, int index) {
			return new TimeSeriesInput(owner, "in" + index);
		}
	}

	class SimpleObject implements InputInstantiator<Object>, Serializable {
		@Override
		public Input<Object> instantiate(AbstractSignalPathModule owner, int index) {
			return new Input<>(owner, "in" + index, "Object");
		}
	}
}
