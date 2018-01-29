package com.unifina.security

import spock.lang.Specification

import java.security.Policy

class UserJavaClassLoaderSpec extends Specification {

	UserJavaClassLoader cl
	
    void setup() {
		Policy.setPolicy(new MyPolicy())
        System.setSecurityManager(new MySecurityManager())
		cl = new UserJavaClassLoader(ClassLoader.getSystemClassLoader())
    }

    void cleanup() {
        System.setSecurityManager(null)
		cl.close()
    }
	  
    void "throws SecurityException if trying to open file"() {
		when:
		parseLoadAndRunInstanceOfCode("""
			public class CustomModule implements Runnable {
				public void run() {
					new java.io.File("C:\\restricted.txt").canRead();
				} 
			}
		""")

		then:
		def e = thrown(SecurityException)
		e.message == "Access denied to java.io.File"
    }
	
	void "throws SecurityException if trying to get parent class loader"() {
		when:
		parseLoadAndRunInstanceOfCode("""
			public class CustomModule implements Runnable { 
				public void run() {
					getClass().getClassLoader().getParent();
				} 
			}
		""")

		then:
		def e = thrown(SecurityException)
		e.message == 'access denied ("java.lang.RuntimePermission" "getClassLoader")'
	}
	
	void "throws SecurityException if trying to get system class loader"() {
		when:
		parseLoadAndRunInstanceOfCode("""
			public class CustomModule implements Runnable { 
				public void run() {
					ClassLoader.getSystemClassLoader();
				} 
			}
		""")

		then:
		def e = thrown(SecurityException)
		e.message == 'access denied ("java.lang.RuntimePermission" "getClassLoader")'
	}
	
	void "throws SecurityException if trying to start a thread"() {
		when:
		parseLoadAndRunInstanceOfCode("""
			public class CustomModule implements Runnable { 
				public void run() {
					new Thread().start();
				} 
			}
		""")

		then:
		def e = thrown(SecurityException)
		e.message == "Access denied to java.lang.Thread"
	}
	
	void "throws SecurityException if trying to open a HTTP connection"() {
		when:
		parseLoadAndRunInstanceOfCode("""
			public class CustomModule { 
				public void run() throws Exception {
					new java.net.URL("http://www.google.fi").openConnection();
				} 
			}
		""")

		then:
		def e = thrown(SecurityException)
		e.message == "Access denied to java.net.URL"
	}


	private void parseLoadAndRunInstanceOfCode(String code) {
		boolean success = cl.parseClass("CustomModule", code)
		assert success
		Class c = cl.loadClass("CustomModule")
		c.newInstance().run()
	}
}
