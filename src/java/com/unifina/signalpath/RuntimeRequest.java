package com.unifina.signalpath;

import com.unifina.domain.security.Permission;
import com.unifina.domain.security.SecUser;
import com.unifina.domain.signalpath.Canvas;
import com.unifina.feed.ITimestamped;

import java.util.*;
import java.util.concurrent.Future;

public class RuntimeRequest extends LinkedHashMap<String, Object> implements ITimestamped {

	private final Date timestamp;

	private final Canvas canvas;
	private final String path;
	private final SecUser user;
	private Future<RuntimeResponse> future = null;
	private Set<Permission.Operation> checkedOperations = new HashSet<>();
	private String originalPath = null;

	public RuntimeRequest(Map<String, Object> msg, SecUser user, Canvas canvas, String path, String originalPath, Set<Permission.Operation> checkedOperations) {
		super();
		this.timestamp = new Date();
		this.user = user;
		this.canvas = canvas;
		this.path = path;
		this.originalPath = originalPath;

		if (msg.get("type")==null)
			throw new IllegalArgumentException("RuntimeRequests must contain the key 'type', with a String value identifying the type of request.");

		for (String key : msg.keySet()) {
			this.put(key, msg.get(key));
		}
		this.checkedOperations.addAll(checkedOperations);
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

	public String getPath() {
		return path;
	}

	public PathReader getPathReader() {
		return RuntimeRequest.getPathReader(getPath());
	}

	public static PathReader getPathReader(String path) {
		return new PathReader(path);
	}

	public Set<Permission.Operation> getCheckedOperations() {
		return checkedOperations;
	}

	public String getOriginalPath() {
		return originalPath;
	}

	public static class PathReader {

		private LinkedList<String> list;
		private String path;

		public PathReader(String path) {
			this.path = path;

			list = new LinkedList<>(Arrays.asList(path.split("/")));
			if (list.size() < 2) {
				throw new IllegalArgumentException("Runtime request path is too short: "+path);
			}
		}

		public boolean isEmpty() {
			return list.isEmpty();
		}

		public String readString(String tokenName) {
			String token = list.poll();
			if (!token.equals(tokenName)) {
				throw new IllegalArgumentException("Unexpected token! Expecting '"+tokenName+"', was: "+token+", path: "+path);
			}
			return list.poll();
		}

		public Long readLong(String tokenName) {
			return Long.parseLong(readString(tokenName));
		}

		public Integer readInt(String tokenName) {
			return Integer.parseInt(readString(tokenName));
		}

		public String readCanvasId() {
			return readString("canvases");
		}

		public Long readDashboardId() {
			return readLong("dashboards");
		}

		public Integer readModuleId() {
			return readInt("modules");
		}

	}

}
