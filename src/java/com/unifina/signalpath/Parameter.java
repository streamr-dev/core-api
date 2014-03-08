package com.unifina.signalpath;

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

	protected T defaultValue;
	
	public Parameter(AbstractSignalPathModule owner, String name, T defaultValue, String typeName) {
		super(owner, name, typeName);
		
		this.defaultValue = defaultValue;
		drivingInput = false;
	}
	
	@Override
	public void receive(Object value) {
		super.receive(value);
		onValue(getValue());
	}
	
	protected void onValue(T value) {
		
	}
	
	/**
	 * Returns the value of this parameter. If there is a current value,
	 * it is returned. Else if the parameter is connected and the source
	 * is pullable, pull a value from the source. Otherwise return the
	 * default value.
	 * @return
	 */
	public T getValue() {
		if (value!=null)
			return value;
		else if (isConnected() && source.owner instanceof Pullable<?>) {
			Object pulledObject =((Pullable<?>)source.owner).pullValue(source); 
			value = handlePulledObject(pulledObject);
			return value;
		}
		else return defaultValue;
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
	protected void doClear() {
		// Parameters need not be cleared
	}
	
	@Override
	public Map<String,Object> getConfiguration() {
		Map<String,Object> config = super.getConfiguration();
		
		config.put("defaultValue",defaultValue);
		
		if (value!=null)
			config.put("value",value);
		else 
			config.put("value",defaultValue);
		
		return config;
	}
	
	@Override
	public void setConfiguration(Map<String,Object> config) {
		super.setConfiguration(config);

		boolean conn = false;
		if (config.containsKey("connected"))
			conn = Boolean.parseBoolean(config.get("connected").toString());
		
 
		if (config.containsKey("value")) {
			T val = parseValue(config.get("value").toString());
			
			// If unconnected, use the value contained in JSON
			if (!conn) {
				receive(val);
			}
			// Otherwise use the value in JSON only as backup value, we want to try to use the lazy pull mechanism
			else {
				value = null;
				defaultValue = val;
			}
			
		}
	}
	
	/**
	 * Must return the value of type <T> represented by the String value s. 
	 * @param s
	 * @return
	 */
	public abstract T parseValue(String s);
	
	@Override
	public boolean hasValue() {
		// Parameters should always (look like they) have a value
		if (defaultValue!=null)
			return true;
		else return super.hasValue();
	}
	
}
