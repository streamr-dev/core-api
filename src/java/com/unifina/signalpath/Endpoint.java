package com.unifina.signalpath;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public abstract class Endpoint<T> implements Serializable {
	public AbstractSignalPathModule owner;
	protected String name;
	protected String displayName;
	protected String typeName;
	private String jsClass;
	
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
	
	public Map<String,Object> resetConfiguration() {
		json = null;
		return getConfiguration();
	}
	
	public Map<String,Object> getConfiguration() {
		Map<String,Object> map = (json != null ? json : new HashMap<String,Object>());
		
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

		return map;
	}

	/**
	 * Returns an array of typenames that this Input accepts.
	 * By default returns an array with one element: the one returned by getTypeName()
	 * @return
	 */
	protected String[] getAcceptedTypes() {
		return getTypeName().split(" ");
	}
	
	public void setConfiguration(Map<String,Object> config) {
		json = new LinkedHashMap<>(config);
		configured = true;
		
		if (config.containsKey("displayName"))
			displayName = (String)config.get("displayName");
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
			aliases = new ArrayList<String>(1);
		aliases.add(name);
	}
	
	public List<String> getAliases() {
		if (aliases==null)
			return new ArrayList<String>(0);
		else return aliases;
	}

	public void setJsClass(String jsClass) {
		this.jsClass = jsClass;
	}

	/**
	 * Clear the state of this Endpoint.
	 */
	public abstract void clear();
}
