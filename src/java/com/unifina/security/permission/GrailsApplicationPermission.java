package com.unifina.security.permission;

import java.security.Permission;

public class GrailsApplicationPermission extends Permission {

	private static final long serialVersionUID = -3539944322286343533L;

	public GrailsApplicationPermission() {
		super("GrailsApplicationPermission");
	}
	
	@Override
	public boolean implies(Permission permission) {
		return false;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof GrailsApplicationPermission;
	}

	@Override
	public int hashCode() {
		return "GrailsApplicationPermission".hashCode();
	}

	@Override
	public String getActions() {
		return "";
	}

}
