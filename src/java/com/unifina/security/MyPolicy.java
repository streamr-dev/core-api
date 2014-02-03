package com.unifina.security;

import java.lang.reflect.ReflectPermission;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.Policy;
import java.security.ProtectionDomain;
import java.util.PropertyPermission;

public class MyPolicy extends Policy {

	private PermissionCollection all;
	private PermissionCollection java;
	private PermissionCollection groovy;
	
	public MyPolicy() {
		all = new Permissions();
		all.add(new AllPermission());

		java = new Permissions();
		java.add(new RuntimePermission("accessDeclaredMembers"));
		java.add(new PropertyPermission("grails.full.stacktrace","read")); // Needed to get proper error msgs?
		java.setReadOnly();
		
		groovy = new Permissions();
		groovy.add(new RuntimePermission("accessDeclaredMembers"));
		groovy.add(new PropertyPermission("grails.full.stacktrace","read")); // Needed to get proper error msgs?
		
		// Only seems to be needed when running in run-app
		if (System.getProperty("unifina.unsafe")!=null) {
			groovy.add(new ReflectPermission("suppressAccessChecks"));
			groovy.add(new RuntimePermission("getClassLoader"));
		}
		
		groovy.setReadOnly();
	}

	@Override
	public PermissionCollection getPermissions(CodeSource codesource) {
		if (codesource.getLocation()!=null && codesource.getLocation().getPath().endsWith("java/untrusted"))
			return java;
		if (codesource.getLocation()!=null && codesource.getLocation().getPath().endsWith("groovy/untrusted"))
			return groovy;
		else return all;
	}
	
	@Override
	public PermissionCollection getPermissions(ProtectionDomain domain) {
		return getPermissions(domain.getCodeSource());
	}
	
}
