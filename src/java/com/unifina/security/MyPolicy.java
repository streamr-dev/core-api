package com.unifina.security;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FilePermission;
import java.lang.reflect.ReflectPermission;
import java.security.*;
import java.util.Enumeration;
import java.util.PropertyPermission;

public class MyPolicy extends Policy {

	private PermissionCollection all;
	private PermissionCollection java;
	private PermissionCollection groovy;
	
	private static final Logger log = Logger.getLogger(MyPolicy.class);
	
	public MyPolicy() {
		log.info("Installing security policy.");
		
		all = new RMICompatibleAllowingPermissionCollection();

		java = new Permissions();
		java.add(new RuntimePermission("accessDeclaredMembers"));
		java.add(new PropertyPermission("grails.full.stacktrace","read")); // Needed to get proper error msgs?
		java.setReadOnly();
		
		groovy = new Permissions();
		groovy.add(new RuntimePermission("accessDeclaredMembers"));
		groovy.add(new PropertyPermission("grails.full.stacktrace","read")); // Needed to get proper error msgs?
		
		if (System.getProperty("catalina.base")!=null && new File(System.getProperty("catalina.base"),"webapps").exists())
			groovy.add(new FilePermission(System.getProperty("catalina.base")+"/webapps/-", "read"));
		else if (System.getProperty("catalina.home")!=null && new File(System.getProperty("catalina.base"),"webapps").exists())
			groovy.add(new FilePermission(System.getProperty("catalina.home")+"/webapps/-", "read"));
		else log.warn("Failed to find webapps directory in order to grant read permissions!");
		
		// Only seems to be needed when running in run-app
		if (System.getProperty("unifina.unsafe")!=null) {
			log.warn("Running in unsafe mode!");
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

	/**
	 * PermissionCollection that allows everything. Basically like adding AllPermissions to a normal
	 * java.security.Permissions collection, but this one works around a stupidity in Sun Java.
	 *
	 * More specifically the problem is in sun.rmi.server.LoaderHandler, which forcefully tries to add
	 * a few Permission objects to a PermissionCollection obtained for CodeSource null, without checking
	 * if it has AllPermissions or whether the PermissionCollection is read only.
	 *
	 * This will lead to failure eg. when trying to use JMX monitoring on the JVM process.
	 */
	class RMICompatibleAllowingPermissionCollection extends PermissionCollection {

		@Override
		public void add(Permission permission) {
			// Do nothing, since this collection already contains all possible permissions
		}

		@Override
		public boolean implies(Permission permission) {
			// Always allow
			return true;
		}

		@Override
		public Enumeration<Permission> elements() {
			return new Enumeration<Permission>() {
				@Override
				public boolean hasMoreElements() {
					return false;
				}

				@Override
				public Permission nextElement() {
					return null;
				}
			};
		}
	}
	
}
