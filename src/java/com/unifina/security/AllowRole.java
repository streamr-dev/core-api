package com.unifina.security;

import com.unifina.domain.security.SecUser;

public enum AllowRole {
	NO_ROLE_REQUIRED,
	DEVOPS,
	ADMIN;

	public boolean hasUserRole(SecUser secUser) {
		switch (this) {
			case NO_ROLE_REQUIRED:
				return true;
			case DEVOPS:
				return secUser != null && secUser.isDevOps();
			case ADMIN:
				return secUser != null && secUser.isAdmin();
		}
		throw new IllegalArgumentException("Unexpected AllowRole: " + this);
	}
}
