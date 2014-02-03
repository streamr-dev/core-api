package com.unifina.security

import static org.junit.Assert.*
import grails.test.mixin.*
import grails.test.mixin.support.*

import java.security.Policy

import org.junit.*

import com.unifina.utils.GlobalsFactory

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
class UserJavaClassLoaderTests {

	UserJavaClassLoader cl
	
    void setUp() {
		Policy.setPolicy(new MyPolicy())
        System.setSecurityManager(new MySecurityManager())
		cl = new UserJavaClassLoader(ClassLoader.getSystemClassLoader())
    }

    void tearDown() {
        System.setSecurityManager(null)
		cl.close()
    }
	//System.exit(0);
	
	  //debug("Can read: "+new java.io.File("C:\\restricted.txt").canRead());
	  
	  //debug("Unsafe classloader: "+getClass().getClassLoader().getParent());
	  
	  /*
	  try {
		((UserJavaClassLoader)getClass().getClassLoader()).parseClass("Foo","class Foo { }");
		Class c = getClass().getClassLoader().loadClass("Foo");
		debug("Foo instance: "+c.newInstance());
	  } catch (Exception e) {
		throw new RuntimeException(e);
	  }*/
	  
	  //debug("App config: "+globals.getGrailsApplication().getConfig());
	  
	  //System.out.println("Foo!");
	  
	  //new Thread().start();
	  
    void testFile() {
		String s = """
			public class CustomModule implements Runnable {
				public void run() {
					new java.io.File("C:\\restricted.txt").canRead();
				} 
			}
		"""
		boolean success = cl.parseClass("CustomModule", s)
		assert success
		Class c = cl.loadClass("CustomModule")
		shouldFail(SecurityException) {
			c.newInstance().run()
		}
    }
	
	void testParentClassLoader() {
		String s = """
			public class CustomModule implements Runnable { 
				public void run() {
					getClass().getClassLoader().getParent();
				} 
			}
		"""
		boolean success = cl.parseClass("CustomModule", s)
		assert success
		Class c = cl.loadClass("CustomModule")
		shouldFail(SecurityException) {
			c.newInstance().run()
		}
	}
	
	void testSystemClassLoader() {
		String s = """
			public class CustomModule implements Runnable { 
				public void run() {
					ClassLoader.getSystemClassLoader();
				} 
			}
		"""
		boolean success = cl.parseClass("CustomModule", s)
		assert success
		Class c = cl.loadClass("CustomModule")
		shouldFail(SecurityException) {
			c.newInstance().run()
		}
	}
	
//	void testParseNewClass() {
//		String s = """
//			import com.unifina.signalpath.custom.UserJavaClassLoader;
//
//			public class CustomModule implements Runnable {
//				public void run() {
//					((UserJavaClassLoader)getClass().getClassLoader()).parseClass("Foo","class Foo { }"); 
//					try {
//						Class c = getClass().getClassLoader().loadClass("Foo");
//						c.newInstance();
//					} catch (Exception e) {}
//				}
//			}
//		"""
//		boolean success = cl.parseClass("CustomModule", s)
//		assert success
//		Class c = cl.loadClass("CustomModule")
//		shouldFail(SecurityException) {
//			c.newInstance().run()
//		}
//	}
	
	void testStartThread() {
		String s = """
			public class CustomModule implements Runnable { 
				public void run() {
					new Thread().start();
				} 
			}
		"""
		boolean success = cl.parseClass("CustomModule", s)
		assert success
		Class c = cl.loadClass("CustomModule")
		shouldFail(SecurityException) {
			c.newInstance().run()
		}
	}
	
	void testURL() {
		String s = """
			public class CustomModule { 
				public void run() throws Exception {
					new java.net.URL("http://www.google.fi").openConnection();
				} 
			}
		"""
		boolean success = cl.parseClass("CustomModule", s)
		assert success
		Class c = cl.loadClass("CustomModule")
		shouldFail(SecurityException) {
			c.newInstance().run()
		}
	}
	
//	void testGlobals() {
//		String s = """
//			import com.unifina.utils.Globals;
//			public class CustomModule { 
//
//				Globals globals;
//
//				public void setGlobals(Globals globals) {
//					this.globals = globals;
//				}
//
//				public void run() throws Exception {
//					globals.getUser();
//				} 
//			}
//		"""
//		boolean success = cl.parseClass("CustomModule", s)
//		assert success
//		Class c = cl.loadClass("CustomModule")
//		def i = c.newInstance();
//		i.setGlobals(GlobalsFactory.createInstance([:], null))
//		shouldFail(SecurityException) {
//			c.newInstance().run()
//		}
//	}
	
}
