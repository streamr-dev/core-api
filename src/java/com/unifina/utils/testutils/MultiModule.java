package com.unifina.utils.testutils;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.TimeSeriesInput;
import com.unifina.signalpath.TimeSeriesOutput;
import com.unifina.signalpath.simplemath.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Example module for testing (de)serialization of several, connected modules.
 */
public class MultiModule extends AbstractSignalPathModule {

	TimeSeriesInput in1 = new TimeSeriesInput(this, "in1");
	TimeSeriesInput in2 = new TimeSeriesInput(this, "in2");

	TimeSeriesOutput out = new TimeSeriesOutput(this, "out");

	private AbstractSignalPathModule addMulti;
	private AbstractSignalPathModule count;
	private AbstractSignalPathModule max;
	private AbstractSignalPathModule invert;
	private AbstractSignalPathModule squareRoot;
	private AbstractSignalPathModule multiply;
	private AbstractSignalPathModule multiply2;
	private AbstractSignalPathModule sum;

	private List<AbstractSignalPathModule> modules = new ArrayList<>();

	@Override
	public void init() {
		super.init();

		addMulti = addModule(new VariadicAddMulti());
		count = addModule(new Count());
		max = addModule(new Max());

		invert = addModule(new Invert());
		squareRoot = addModule(new SquareRoot());

		multiply = addModule(new Multiply());

		multiply2 = addModule(new Multiply());

		sum = addModule(new Sum());

		addMulti.getOutput("sum").connect(invert.getInput("in"));

		count.getOutput("count").connect(multiply.getInput("B"));
		invert.getOutput("out").connect(multiply.getInput("A"));

		max.getOutput("out").connect(squareRoot.getInput("in"));

		multiply.getOutput("A*B").connect(multiply2.getInput("B"));
		squareRoot.getOutput("sqrt").connect(multiply2.getInput("A"));

		multiply2.getOutput("A*B").connect(sum.getInput("in"));
	}

	@Override
	public void initialize() {
		for (AbstractSignalPathModule module : modules) {
			module.initialize();
		}
	}

	private AbstractSignalPathModule addModule(AbstractSignalPathModule module) {
		module.init();
		module.configure(module.getConfiguration());
		modules.add(module);
		return module;
	}

	@Override
	public void sendOutput() {
		Double v1 = in1.getValue();
		Double v2 = in2.getValue();

		addMulti.getInput("in1").receive(v1);
		addMulti.getInput("in2").receive(v2);

		count.getInput("in").receive(v1);

		max.getInput("A").receive(v1);
		max.getInput("B").receive(v2);

		for (AbstractSignalPathModule module : modules) {
			module.trySendOutput();
		}

		out.send((Double) sum.getOutput("out").getValue());

	}

	@Override
	public void clearState() {
		for (AbstractSignalPathModule module : modules) {
			module.clearState();
		}
	}
}
