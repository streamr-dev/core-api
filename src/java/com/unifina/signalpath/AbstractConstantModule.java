package com.unifina.signalpath;

/**
 * Abstraction for modules that act as a constant of a certain type.
 * These modules contain one Parameter of the type in question, plus
 * an output of that type. The Parameter is always driving and can't
 * be made non-driving (it would make no sense). The module supports
 * initial value pull (as opposed to sending a value on activation).
 *
 * @param <ValueType> The constant type
 */
public abstract class AbstractConstantModule<ValueType> extends AbstractSignalPathModule implements Pullable<ValueType> {

	private Parameter<ValueType> constant;
	private Output<ValueType> out;

	public AbstractConstantModule() {
		super();
		initPriority = 40;
		constant = createConstantParameter();
		out = createOutput();
	}

	protected abstract Parameter<ValueType> createConstantParameter();
	protected abstract Output<ValueType> createOutput();

	@Override
	public void init() {
		super.init();
		constant.canToggleDrivingInput = false;
		constant.setDrivingInput(true);
	}

	@Override
	public void initialize() {
		if (!constant.isConnected() || constant.isReady()) {
			// Send out the value to any connected inputs to mark them ready.
			// Otherwise the receiving module won't be able to activate.
			out.send(constant.getValue());
		}
	}

	@Override
	public void sendOutput() {
		out.send(constant.getValue());
	}

	@Override
	public void clearState() {}

	@Override
	public ValueType pullValue(Output output) {
		return constant.getValue();
	}
}
