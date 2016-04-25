package com.unifina.signalpath.variadic;

import com.unifina.signalpath.*;

import java.io.Serializable;

/**
 * Used by <code>VariadicOutput</code> to instantiate output of certain type with pre-determined settings.
 */
public interface OutputInstantiator<T> {
	Output<T> instantiate(AbstractSignalPathModule owner, int index);

	class TimeSeries implements OutputInstantiator<Double>, Serializable {
		@Override
		public Output<Double> instantiate(AbstractSignalPathModule owner, int index) {
			return new TimeSeriesOutput(owner, "out" + index);
		}
	}

	class SimpleObject implements OutputInstantiator<Object>, Serializable {
		@Override
		public Output<Object> instantiate(AbstractSignalPathModule owner, int index) {
			return new Output<>(owner, "out" + index, "Object");
		}
	}

}
