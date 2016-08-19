package com.unifina.signalpath;

import com.unifina.utils.IdGenerator;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.*;

public abstract class Endpoint<T> implements Serializable {
	public AbstractSignalPathModule owner;
	protected String name;
	protected String displayName;
	protected String typeName;
	private String jsClass;
	private String id = IdGenerator.get();
	
	private Map<String,Object> json;
	private boolean configured = false;
	
	protected boolean canConnect = true;
	protected List<String> aliases = null;
	
	public Endpoint(AbstractSignalPathModule owner, String name, String typeName) {
		this.owner = owner;
		this.name = name;
		this.typeName = typeName;
	}

	public AbstractSignalPathModule getOwner() {
		return owner;
	}

	public void setOwner(AbstractSignalPathModule owner) {
		this.owner = owner;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	public void setDisplayName(String s) {
		displayName = s;
	}
	
	public String getTypeName() {
		return typeName;
	}

	public Class<T> getTypeClass() {
		Class clazz = this.getClass();
		while (!(clazz.getGenericSuperclass() instanceof ParameterizedType)) {
			clazz = this.getClass().getSuperclass();
		}
		ParameterizedType pt = (ParameterizedType) clazz.getGenericSuperclass();
		Class firstTypeArgument = (Class) pt.getActualTypeArguments()[0];
		return firstTypeArgument;
	}
	
	public String getLongName() {
		return (getOwner()!=null ? getOwner().getName()+"." : "") + getEffectiveName();
	}

	public String getEffectiveName() {
		return getDisplayName() != null ? getDisplayName() : getName();
	}
	
	public abstract boolean isConnected();
	
	public void regenerateId() {
		id = IdGenerator.get();
	}

	public Map<String,Object> getConfiguration() {
		Map<String,Object> map = (json != null ? json : new HashMap<String,Object>());

		map.put("id", id);
		map.put("name", name);
		map.put("longName", owner.getName()+"."+name);
		map.put("type",getTypeName());
		map.put("connected", isConnected());
		map.put("canConnect", canConnect);

		if (displayName != null) {
			map.put("displayName", displayName);
		}
		if (jsClass != null) {
			map.put("jsClass", jsClass);
		}
		if (getValue() != null) {
			map.put("value", getValue());
		}

		return map;
	}
	
	public void setConfiguration(Map<String,Object> config) {
		json = new LinkedHashMap<>(config);
		configured = true;
		
		if (config.containsKey("displayName")) {
			displayName = (String) config.get("displayName");
		}

		if (config.containsKey("id")) {
			id = config.get("id").toString();
		}
	}
	
	@Override
	public String toString() {
		return owner.getName()+"."+name;
	}
	

	public boolean isConfigured() {
		return configured;
	}
	
	/**
	 * Adds other names that this input can be found by.
	 * Aliases must be added before calling {@link AbstractSignalPathModule#addInput(Input)} 
	 * or {@link AbstractSignalPathModule#addOutput(Output)}.
	 * @param name
	 */
	public void addAlias(String name) {
		if (aliases==null)
			aliases = new ArrayList<>(1);
		aliases.add(name);
	}
	
	public List<String> getAliases() {
		if (aliases==null)
			return new ArrayList<>(0);
		else return aliases;
	}

	public void setJsClass(String jsClass) {
		this.jsClass = jsClass;
	}

	/**
	 * Returns the most recent value at this Endpoint.
     */
	public abstract T getValue();

	/**
	 * Clear the state of this Endpoint.
	 */
	public abstract void clear();

	/**
	 * Disconnects this Endpoint.
	 */
	public abstract void disconnect();
}
