package com.unifina.signalpath;

import com.unifina.domain.security.SecUser;
import com.unifina.domain.signalpath.Canvas;
import com.unifina.feed.ITimestamped;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Future;

public class RuntimeRequest extends LinkedHashMap<String, Object> implements ITimestamped {

	private final Date timestamp;

	private final Canvas canvas;
	private final SecUser user;
	Future<RuntimeResponse> future = null;
	
	public RuntimeRequest(Map<String, Object> msg, SecUser user, Canvas canvas) {
		super();
		this.timestamp = new Date();
		this.user = user;
		this.canvas = canvas;

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
		return user != null;
	}

	@Override
	public Date getTimestamp() {
		return timestamp;
	}

	public Canvas getCanvas() {
		return canvas;
	}

	public SecUser getUser() {
		return user;
	}
}
