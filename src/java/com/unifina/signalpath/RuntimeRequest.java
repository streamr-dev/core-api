package com.unifina.signalpath;

import com.unifina.feed.ITimestamped;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Future;

public class RuntimeRequest extends LinkedHashMap<String, Object> implements ITimestamped {

	private final Date timestamp;
	Future<RuntimeResponse> future = null;
	boolean authenticated = false;
	
	public RuntimeRequest(Map<String, Object> msg, Date timestamp) {
		super();
		this.timestamp = timestamp;

		if (msg.get("type")==null)
			throw new IllegalArgumentException("RuntimeRequests must contain the key 'type', with a String value identifying the type of request.");
		
		for (String key : msg.keySet()) {
			this.put(key, msg.get(key));
		}
	}
	
	public String getType() {
		return this.get("type").toString();
	}

	public Future<RuntimeResponse> getFuture() {
		return future;
	}

	public void setFuture(Future<RuntimeResponse> future) {
		this.future = future;
	}

	public boolean isAuthenticated() {
		return authenticated;
	}

	public void setAuthenticated(boolean authenticated) {
		this.authenticated = authenticated;
	}

	@Override
	public Date getTimestamp() {
		return timestamp;
	}
}
