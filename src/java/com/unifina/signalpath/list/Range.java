package com.unifina.signalpath.list;

import com.unifina.signalpath.*;

import java.util.ArrayList;
import java.util.List;

public class Range extends AbstractSignalPathModule implements Pullable<List<Double>> {
	private static final double DELTA = 0.000000001;

	private final DoubleParameter from = new DoubleParameter(this, "from", 0d);
	private final DoubleParameter to = new DoubleParameter(this, "to", 100d);
	private final DoubleParameter step = new AbsDoubleParameter(this, "step", 1d);
	private final ListOutput out = new ListOutput(this, "out");

	private double currentFrom = from.getValue();
	private double currentTo = to.getValue();
	private double currentStep = step.getValue();
	private List<Double> sequence = buildSequence(currentFrom, currentTo, currentStep);

	@Override
	public void initialize() {
		// Send out the value to any connected inputs to mark them ready.
		// Otherwise the receiving module won't be able to activate.
		sendOutput();
	}

	@Override
	public void sendOutput() {
		updateSequenceIfNeeded();
		out.send(sequence);
	}

	@Override
	public void clearState() {}

	@Override
	public List<Double> pullValue(Output output) {
		return sequence;
	}

	private void updateSequenceIfNeeded() {
		boolean paramsChanged = Math.abs(currentFrom - from.getValue()) > DELTA ||
			Math.abs(currentTo - to.getValue()) > DELTA ||
			Math.abs(currentStep - step.getValue()) > DELTA;

		if (paramsChanged) {
			currentFrom = from.getValue();
			currentTo = to.getValue();
			currentStep = step.getValue();
			sequence = buildSequence(currentFrom, currentTo, currentStep);
		}
	}

	private static List<Double> buildSequence(Double from, Double to, Double step) {
		List<Double> sequence = new ArrayList<>();
		boolean increasing = to - from >= 0d;
		if (Math.abs(step) > DELTA) {
			if (increasing) {
				while (from <= to) {
					sequence.add(from);
					from += step;
				}
			} else {
				while (from >= to) {
					sequence.add(from);
					from -= step;
				}
			}
		}
		return sequence;
	}

	private static class AbsDoubleParameter extends DoubleParameter {
		private AbsDoubleParameter(AbstractSignalPathModule owner, String name, Double defaultValue) {
			super(owner, name, defaultValue);
		}

		@Override
		public Double getValue() {
			return Math.abs(super.getValue());
		}
	}

}
