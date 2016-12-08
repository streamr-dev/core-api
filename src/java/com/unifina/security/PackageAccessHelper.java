package com.unifina.security;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PackageAccessHelper {
	
	private static final Set<String> restrictedClasses = new HashSet<>();
	
	static {
		restrictedClasses.add("java.lang.Thread");
		restrictedClasses.add("java.lang.Runtime");
	}
	
	private static final String[] allowedPackages = new String[] {
			"CustomModule",
			"java.lang.",
			"java.math.",
			"java.util.",
			
			"java.text.SimpleDateFormat",
			
			"com.unifina.signalpath.",
			"com.unifina.orderbook.event.",
			"com.unifina.order.",
			"com.unifina.orderbook.",
			"com.unifina.utils.",
			"com.unifina.event.",
			
			"org.apache.commons.math3",
			"org.springsource.loaded.TypeRegistry", // safe?
			"sun.reflect.SerializationConstructorAccessorImpl", // safe?
			"org.codehaus.groovy.grails.web.json."
	};
	
	private static boolean checkMatches(String className, String[] packages) {
    	for (String s : packages)
    		if (className.startsWith(s))
    			return true;
    	return false;
	}
	
    public static boolean checkAccess(String className, String codeBase) {
    	// Matches for restricted packages are checked in Java security system, if listed in packages.access security property
    	if (restrictedClasses.contains(className) || !checkMatches(className, allowedPackages))
    		throw new SecurityException("Access denied to "+className);
    	else return true;
    } 
    
    public static List<String> getRestrictedPackages() {
    	return Arrays.asList(restrictedClasses.toArray(new String[restrictedClasses.size()]));
    }
    
    public static List<String> getAllowedPackages() {
    	return Arrays.asList(allowedPackages);
    }
    
}
