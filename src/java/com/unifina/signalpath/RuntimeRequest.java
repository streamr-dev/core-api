package com.unifina.signalpath;

import com.unifina.domain.Permission;
import com.unifina.domain.User;
import com.unifina.domain.Canvas;
import com.streamr.client.protocol.message_layer.ITimestamped;
import com.unifina.security.TokenAuthenticator.AuthorizationHeader;

import java.util.*;
import java.util.concurrent.Future;

public class RuntimeRequest extends LinkedHashMap<String, Object> implements ITimestamped {

	private final Date timestamp;

	private final Canvas canvas;
	private final String path;
	private final User user;
	private final AuthorizationHeader authorizationHeader;  // TODO should we remove the "user" field in the future as we can always find the user by this authorization?
	private Future<RuntimeResponse> future = null;
	private Set<Permission.Operation> checkedOperations = new HashSet<>();
	private String originalPath = null;

	public RuntimeRequest(Map<String, Object> msg, User user, AuthorizationHeader authorizationHeader, Canvas canvas, String path, String originalPath, Set<Permission.Operation> checkedOperations) {
		super();
		this.timestamp = new Date();
		this.user = user;
		this.authorizationHeader = authorizationHeader;
		this.canvas = canvas;
		this.path = path;
		this.originalPath = originalPath;

		if (msg.get("type") == null) {
			throw new IllegalArgumentException("RuntimeRequests must contain the key 'type', with a String value identifying the type of request.");
		}

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
	public Date getTimestampAsDate() {
		return timestamp;
	}

	public Canvas getCanvas() {
		return canvas;
	}

	public User getUser() {
		return user;
	}

	public AuthorizationHeader getAuthorizationHeader() {
		return authorizationHeader;
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

		public String readDashboardId() {
			return readString("dashboards");
		}

		public String readCanvasId() {
			return readString("canvases");
		}

		public Integer readModuleId() {
			return readInt("modules");
		}

	}

	public static class PathWriter {
		private String path = "";

		public PathWriter write(String s) {
			path += s;
			return this;
		}

		public PathWriter writeCanvasId(String id) {
			return write("/canvases/" + id);
		}

		public PathWriter writeDashboardId(Long id) {
			return write("/dashboards/"+id);
		}

		public PathWriter writeModuleId(Integer id) {
			return write("/modules/" + id);
		}

		@Override
		public String toString() {
			return path;
		}
	}

}
