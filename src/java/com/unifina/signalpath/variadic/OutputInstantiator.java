package com.unifina.signalpath.variadic;

import com.unifina.signalpath.*;

import java.io.Serializable;

/**
 * Used by <code>VariadicOutput</code> to instantiate output of certain type with pre-determined settings.
 */
public interface OutputInstantiator<T> {
	Output<T> instantiate(AbstractSignalPathModule owner);

	class TimeSeries implements OutputInstantiator<Double>, Serializable {
		@Override
		public Output<Double> instantiate(AbstractSignalPathModule owner) {
			return new TimeSeriesOutput(owner, null);
		}
	}

	class SimpleObject implements OutputInstantiator<Object>, Serializable {
		@Override
		public Output<Object> instantiate(AbstractSignalPathModule owner) {
			return new Output<>(owner, null, "Object");
		}
	}

}
