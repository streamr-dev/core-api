package com.unifina.controller;

import com.unifina.domain.User;

public enum AllowRole {
	NO_ROLE_REQUIRED,
	DEVOPS,
	ADMIN;

	public boolean hasUserRole(User user) {
		switch (this) {
			case NO_ROLE_REQUIRED:
				return true;
			case DEVOPS:
				return user != null && user.isDevOps();
			case ADMIN:
				return user != null && user.isAdmin();
		}
		throw new IllegalArgumentException("Unexpected AllowRole: " + this);
	}
}
