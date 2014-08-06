package com.unifina.security;


public class MySecurityManager extends SecurityManager {
	@Override
	public void checkAccess(ThreadGroup g) {
		// Always check modify threadgroup permission
		checkPermission(new RuntimePermission("modifyThreadGroup"));
	}
}
