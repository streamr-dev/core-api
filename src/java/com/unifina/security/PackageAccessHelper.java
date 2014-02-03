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
	
//	private static final String[] restrictedPackages = new String[] {
////		"java.lang.reflect", // required by Groovy. safe?
//		"java.lang.Thread",
//		"java.lang.Runtime"
//	};
	
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
			"org.springsource.loaded.TypeRegistry" // safe?
	};
	
	// Most of these are probably unsafe because Groovy allows very liberal
	// access to private fields etc.
	private static final String[] groovyPackages = new String[] {
		"groovy.lang.", // required by Groovy. safe?
		"groovy.util.", // required by Groovy. safe?
		"org.codehaus.groovy.runtime.", // required by Groovy. safe?
		"org.codehaus.groovy.reflection.ClassInfo", // required by Groovy. safe?
		
		"sun.reflect.MethodAccessorImpl", // probably very UNSAFE. needed outside run-app, too?
		"sun.reflect.ConstructorAccessorImpl" // probably very UNSAFE. needed outside run-app, too?
	};

	private static boolean checkMatches(String className, String[] packages) {
    	for (String s : packages)
    		if (className.startsWith(s))
    			return true;
    	return false;
	}
	
    public static boolean checkAccess(String className, String codeBase) {
    	// Matches for restricted packages are checked in Java security system, if listed in packages.access security property
    	if (restrictedClasses.contains(className)
    		|| codeBase.endsWith("java/untrusted") && !checkMatches(className, allowedPackages)
    		|| codeBase.endsWith("groovy/untrusted") && !(checkMatches(className, allowedPackages) ||  checkMatches(className, groovyPackages)))
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
