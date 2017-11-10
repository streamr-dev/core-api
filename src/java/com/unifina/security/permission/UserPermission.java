package com.unifina.security.permission;

import java.security.Permission;

public class UserPermission extends Permission {

	private static final long serialVersionUID = 3902064687942902085L;

	public UserPermission() {
		super("UserPermission");
	}
	
	@Override
	public boolean implies(Permission permission) {
		return false;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof UserPermission;
	}

	@Override
	public int hashCode() {
		return "UserPermission".hashCode();
	}

	@Override
	public String getActions() {
		return "";
	}
}
