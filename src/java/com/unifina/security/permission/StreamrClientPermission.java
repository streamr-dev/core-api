package com.unifina.security.permission;

import java.security.Permission;

public class StreamrClientPermission extends Permission {

	private static final long serialVersionUID = 41249824197214897L;

	public StreamrClientPermission() {
		super("StreamrClientPermission");
	}

	@Override
	public boolean implies(Permission permission) {
		return false;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof StreamrClientPermission;
	}

	@Override
	public int hashCode() {
		return "StreamrClientPermission".hashCode();
	}

	@Override
	public String getActions() {
		return "";
	}
}
