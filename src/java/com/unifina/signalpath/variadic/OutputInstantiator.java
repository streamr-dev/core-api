package com.unifina.signalpath.variadic;

import com.unifina.signalpath.*;

import java.io.Serializable;

/**
 * Used by <code>VariadicOutput</code> to instantiate output of certain type with pre-determined settings.
 */
public interface OutputInstantiator<T> extends EndpointInstantiator<Output<T>> {

	class TimeSeries implements OutputInstantiator<Double>, Serializable {
		@Override
		public Output<Double> instantiate(AbstractSignalPathModule owner, String outputName) {
			return new TimeSeriesOutput(owner, outputName);
		}
	}

	class SimpleObject implements OutputInstantiator<Object>, Serializable {
		@Override
		public Output<Object> instantiate(AbstractSignalPathModule owner, String outputName) {
			return new Output<>(owner, outputName, "Object");
		}
	}

}
