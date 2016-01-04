package com.unifina.signalpath;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Endpoint<T> implements Serializable {
	public AbstractSignalPathModule owner;
	protected String name;
	protected String displayName;
	protected String typeName;
	
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
	
	public String getLongName() {
		return (getOwner()!=null ? getOwner().getName()+"." : "") + (getDisplayName()!=null ? getDisplayName() : getName());
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
		
		if (displayName!=null)
			map.put("displayName",displayName);
		
		return map;
	}
	
	public void setConfiguration(Map<String,Object> config) {
		json = config;
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
	
}
