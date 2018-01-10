package com.unifina.security.permission;

import java.security.Permission;

public class ConnectionTraversalPermission extends Permission {

	private static final long serialVersionUID = 3902064687942902085L;

	public ConnectionTraversalPermission() {
		super("ConnectionTraversalPermission");
	}
	
	@Override
	public boolean implies(Permission permission) {
		return false;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof ConnectionTraversalPermission;
	}

	@Override
	public int hashCode() {
		return "ConnectionTraversalPermission".hashCode();
	}

	@Override
	public String getActions() {
		return "";
	}
}
