package com.unifina.signalpath;

@Deprecated
public abstract class DynamicInputSignalPathModule extends AbstractSignalPathModule {

	@Override
	public Input getInput(String name) {
		if (super.getInput(name)==null) {
			Input i = createInput(name);
			addInput(i);
			return i;
		}
		else return super.getInput(name);
	}

	protected abstract Input createInput(String name);
	
}
