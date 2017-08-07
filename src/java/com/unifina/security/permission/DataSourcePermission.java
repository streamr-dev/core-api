package com.unifina.security.permission;

import java.security.Permission;

public class DataSourcePermission extends Permission {

	private static final long serialVersionUID = 3902064687942902085L;

	public DataSourcePermission() {
		super("DataSourcePermission");
	}
	
	@Override
	public boolean implies(Permission permission) {
		return false;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof DataSourcePermission;
	}

	@Override
	public int hashCode() {
		return "DataSourcePermission".hashCode();
	}

	@Override
	public String getActions() {
		return "";
	}
}
