package com.unifina.signalpath;

import java.util.List;
import java.util.Map;

/**
 * Parameters are Inputs that always have a value. The user can enter that value in the UI
 * and a textual representation of that value must be available. Parameters can be connected
 * to Outputs, in which case they should behave similar to normal Inputs (but they are not
 * meant to be driving inputs).
 *  
 * @author Henri
 *
 * @param <T>
 */
public abstract class Parameter<T> extends Input<T> {

	private T defaultValue;
	private boolean canBeEmpty = true;
	private boolean updateOnChange = false;
	
	public Parameter(AbstractSignalPathModule owner, String name, T defaultValue, String typeName) {
		super(owner, name, typeName);
		setDrivingInput(false);
		setRequiresConnection(false);
		this.defaultValue = defaultValue;
	}
	
	/**
	 * Returns the value of this parameter. If there is a current value,
	 * it is returned. Else if the parameter is connected and the source
	 * is pullable, pull a value from the source. Otherwise return the
	 * default value.
	 * @return
	 */
	@Override
	public T getValue() {
		if (value != null) {
			return value;
		} else {
			if (isConnected() && getSource().getOwner() instanceof Pullable<?>) {
				Object pulledObject = ((Pullable<?>) getSource().getOwner()).pullValue(getSource());
				value = handlePulledObject(pulledObject);
				checkEmpty(value);
				return value;
			} else {
				checkEmpty(defaultValue);
				return defaultValue;
			}
		}
	}
	
	private void checkEmpty(T v) {
		// Also check the existence of a DataSource, because an empty
		// but required parameter is only a problem when actually running
		// the path (not when creating, loading or saving)
		AbstractSignalPathModule owner = getOwner();
		if (!canBeEmpty && owner.getGlobals() != null && owner.getGlobals().isRunContext() && isEmpty(v)) {
			owner.getParentSignalPath().pushToUiChannel(new ModuleWarningMessage("Parameter "+getDisplayName()+" does not have a value!", owner.hash));
			throw new IllegalArgumentException("Parameter "+(getOwner()!=null ? getOwner().getName()+"." : "")+(getDisplayName()==null ? getName() : getDisplayName())+" does not have a value!");
		}
	}
	
	protected boolean isEmpty(T value) {
		return value == null;
	}

	/**
	 * If the pulled object is not necessarily an instance of T, this method
	 * should be overridden in a subclass to handle that situation (for example,
	 * an IntegerParameter might pull a Double from Constant).
	 * @param o
	 * @return
	 */
	protected T handlePulledObject(Object o) {
		return (T) o;
	}
	
	@Override
	public void clear() {
		// Parameters cannot be cleared - with the exception of connected Parameters, which behave like ordinary Inputs
		if (isConnected()) {
			super.clear();
		}
	}
	
	@Override
	public Map<String,Object> getConfiguration() {
		Map<String,Object> config = super.getConfiguration();

		List<PossibleValue> range = getPossibleValues();
		if (range != null) {
			config.put("possibleValues", range);
		}

		config.put("defaultValue", formatValue(defaultValue));

		if (value != null) {
			config.put("value", formatValue(value));
		} else {
			config.put("value", formatValue(defaultValue));
		}
		
		if (updateOnChange) {
			config.put("updateOnChange", true);
		}
		
		return config;
	}

	/** Subclasses can provide list of values that will be rendered as a drop-down box */
	protected List<PossibleValue> getPossibleValues() {
		return null;
	}
	
	@Override
	public void setConfiguration(Map<String,Object> config) {
		super.setConfiguration(config);

		boolean conn = false;
		if (config.containsKey("connected")) {
			conn = Boolean.parseBoolean(config.get("connected").toString());
		}
 
		if (config.containsKey("value")) {
			// Check config value type and directly assign if possible
			T val;
			Object configValue = config.get("value");
			if (configValue == null) {
				val = null;
			} else {
				Class typeClass = getTypeClass();
				if (typeClass.isAssignableFrom(configValue.getClass())) {
					val = (T) configValue;
				} else {
					// Fall back to parsing
					val = parseValue(configValue.toString());
				}
			}
			
			// If unconnected, use the value contained in JSON
			if (!conn) {
				receive(val);
			} else {
				// Otherwise use the value in JSON only as backup value, we want to try to use the lazy pull mechanism
				value = null;
				defaultValue = val;
			}
		}
	}
	
	public void setDefaultValue(T value) {
		this.defaultValue = value;
	}
	
	/**
	 * Must return the value of type <T> represented by the String value s.
	 * @param s
	 * @return
	 */
	public abstract T parseValue(String s);

	/**
	 * Defines how to format value when writing config
	 */
	public Object formatValue(T value) {
		return value;
	}

	@Override
	public boolean hasValue() {
		return defaultValue != null || super.hasValue();
	}

	public void setUpdateOnChange(boolean updateOnChange) {
		this.updateOnChange = updateOnChange;
	}
}
