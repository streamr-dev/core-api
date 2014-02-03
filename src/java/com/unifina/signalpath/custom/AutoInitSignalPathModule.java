package com.unifina.signalpath.custom;

import java.lang.reflect.Field;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.Input;
import com.unifina.signalpath.Output;

public abstract class AutoInitSignalPathModule extends AbstractSignalPathModule {

	@Override
	public void init() {
			Field[] fields = getClass().getDeclaredFields();
			for (Field f : fields) {
				try {
					Object obj = f.get(this);
					if (Input.class.isInstance(obj))
						addInput((Input)obj);
					else if (Output.class.isInstance(obj))
						addOutput((Output)obj);
				} catch (Exception e) {
					// Ignore exception
				}
			}

	}

}
