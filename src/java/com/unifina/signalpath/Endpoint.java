package com.unifina.signalpath;

import com.unifina.utils.IdGenerator;
import com.unifina.utils.MapTraversal;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.*;

public abstract class Endpoint<T> implements Serializable {
	private AbstractSignalPathModule owner;
	private String name;
	private String displayName;
	private String typeName;
	private String jsClass;
	private String id = "ep_" + IdGenerator.getShort();
	private Map<String, Object> variadicConfig;

	private boolean configured = false;
	private boolean export = false;
	private boolean canConnect = true;
	private List<String> aliases = null;
	
	public Endpoint(AbstractSignalPathModule owner, String name, String typeName) {
		this.owner = owner;
		this.name = name;
		this.typeName = typeName;
	}

	String getId() {
		return id;
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

	public boolean isExport() {
		return export;
	}

	public void setExport(boolean export) {
		this.export = export;
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

	public void setCanConnect(boolean canConnect) {
		this.canConnect = canConnect;
	}

	public boolean isCanConnect() {
		return canConnect;
	}

	public void regenerateId() {
		id = IdGenerator.get();
	}

	public Map<String,Object> getConfiguration() {
		Map<String, Object> map = new LinkedHashMap<>();

		map.put("id", id);
		map.put("name", name);
		map.put("longName", getLongName()); // generated
		map.put("type", typeName);
		map.put("connected", isConnected()); // generated
		map.put("canConnect", canConnect);
		map.put("export", export);

		// Avoid writing null keys for efficiency
		if (displayName != null) {
			map.put("displayName", displayName);
		}
		if (jsClass != null) {
			map.put("jsClass", jsClass);
		}
		if (variadicConfig != null) {
			map.put("variadic", variadicConfig);
		}
		if (getValue() != null) {
			map.put("value", getValue());
		}

		return map;
	}

	public void setConfiguration(Map<String,Object> config) {
		configured = true;

		if (config.containsKey("id")) {
			id = MapTraversal.getString(config, "id");
		}
		// skip name: already set by constructor
		// skip longName: it's generated
		// skip type: already set by constructor
		// skip connected: it's generated
		// skip canConnect: read-only
		if (config.containsKey("export")) {
			export = MapTraversal.getBoolean(config, "export");
		}
		if (config.containsKey("displayName")) {
			displayName = MapTraversal.getString(config, "displayName");
		}
		// skip jsClass: read-only
		if (config.containsKey("variadic")) {
			variadicConfig = MapTraversal.getMap(config, "variadic");
		}
		// skip value: not supposed to read it from config
	}

	/**
	 * Returns an array of typenames that this Input accepts.
	 * By default returns an array with one element: the one returned by getTypeName()
	 * @return
	 */
	protected String[] getAcceptedTypes() {
		return getTypeName().split(" ");
	}

	@Override
	public String toString() {
		return owner.getName() + "." + name;
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
		if (aliases == null) {
			aliases = new ArrayList<>(1);
		}
		aliases.add(name);
	}
	
	public List<String> getAliases() {
		if (aliases == null) {
			return new ArrayList<>(0);
		} else {
			return aliases;
		}
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
