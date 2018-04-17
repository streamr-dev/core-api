package com.unifina.security;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.SourceUnit;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashSet;

/**
 * Used to compile and load user-defined Groovy scripts at runtime.
 * All code is assigned to the codeBase "/groovy/untrusted" so that
 * proper Permissions can be granted in a Policy.
 * @author Henri
 *
 */
public class UserClassLoader extends GroovyClassLoader {
	
	private static final String codeBase = "/groovy/untrusted";
	
	private final HashSet<String> parsedClasses = new HashSet<>();
	
	public UserClassLoader(ClassLoader parent) {
		super(parent);
	}
	
    /**
     * Parses the given text into a Java class capable of being run
     *
     * @param text     the text of the script/class to parse
     */
    public Class parseClass(final String text, final String fileName) throws CompilationFailedException {
        GroovyCodeSource gcs = AccessController.doPrivileged(new PrivilegedAction<GroovyCodeSource>() {
            public GroovyCodeSource run() {
                return new GroovyCodeSource(text, fileName, codeBase);
            }
        });
        gcs.setCachable(false);
        Class c = parseClass(gcs);
        parsedClasses.add(c.getName());
        return c;
    }
    
    @Override
    protected ClassCollector createCollector(CompilationUnit unit, SourceUnit su) {
        InnerLoader loader = AccessController.doPrivileged(new PrivilegedAction<InnerLoader>() {
            public SecureInnerLoader run() {
                return new SecureInnerLoader(UserClassLoader.this);
            }
        });
        return new ModuleClassCollector(loader, unit, su);
    }
    
    
    public class ModuleClassCollector extends GroovyClassLoader.ClassCollector {
		protected ModuleClassCollector(InnerLoader cl, CompilationUnit unit, SourceUnit su) {
			super(cl,unit,su);
		}
    }

    public class SecureInnerLoader extends GroovyClassLoader.InnerLoader {

    	private static final String codeBase = "/groovy/untrusted";
    	
		public SecureInnerLoader(GroovyClassLoader delegate) {
			super(delegate);
		}
		
		@Override
		public Class loadClass(String name, boolean lookupScriptFiles,
				boolean preferClassOverScript, boolean resolve)
				throws ClassNotFoundException, CompilationFailedException {
			PackageAccessHelper.checkAccess(name, codeBase);
			return super.loadClass(name, lookupScriptFiles, preferClassOverScript, resolve);
		}
		
		@Override
		public Class parseClass(File file) throws CompilationFailedException,
				IOException {
			throw new SecurityException("Parsing a class is forbidden.");
		}
		
		@Override
		public Class parseClass(GroovyCodeSource codeSource)
				throws CompilationFailedException {
			throw new SecurityException("Parsing a class is forbidden.");
		}
		
		@Override
		public Class parseClass(GroovyCodeSource codeSource, boolean shouldCache)
				throws CompilationFailedException {
			throw new SecurityException("Parsing a class is forbidden.");
		}
		
		@Override
		public Class parseClass(InputStream in, String fileName)
				throws CompilationFailedException {
			throw new SecurityException("Parsing a class is forbidden.");
		}
		
		@Override
		public Class parseClass(String text) throws CompilationFailedException {
			throw new SecurityException("Parsing a class is forbidden.");
		}
		
		@Override
		public Class parseClass(String text, String fileName)
				throws CompilationFailedException {
			throw new SecurityException("Parsing a class is forbidden.");
		}
		
    }
    
}
