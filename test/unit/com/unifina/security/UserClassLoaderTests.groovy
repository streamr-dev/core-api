package com.unifina.security

import static org.junit.Assert.*
import grails.test.mixin.*
import grails.test.mixin.support.*

import java.security.Policy

import org.junit.*

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
class UserClassLoaderTests {

	UserClassLoader cl
	
    void setUp() {
		Policy.setPolicy(new MyPolicy())
        System.setSecurityManager(new MySecurityManager())
		cl = new UserClassLoader(ClassLoader.getSystemClassLoader())
    }

    void tearDown() {
        System.setSecurityManager(null)
		for (Class c : cl.getLoadedClasses()) {
			GroovySystem.getMetaClassRegistry().removeMetaClass(c);
		}
		cl.close()
    }

	/*
  
  println("You should not be seeing this!")
  //debug("You should see this!")
  
  //debug(new URL("http://www.google.fi").getText())
  //System.out.println("Foo")
  //System.exit(0)
  
  debug(globals.time.toString())
  
  GroovyClassLoader gcl = this.class.classLoader
  Class foo = gcl.parseClass("class Foo { }","Foo")
  foo.newInstance()
  
  globals.grailsApplication.config.each {try {debug(it?.toString())} catch (Exception e) {}}
  
  globals.grailsApplication.config["grails"]["plugins"]["springsecurity"]["interceptUrlMap"]['/user/**'] = ['IS_AUTHENTICATED_ANONYMOUSLY']
  debug("Success!")
  
  def springSecurityService = globals.grailsApplication.mainContext.getBean("springSecurityService")
  com.unifina.security.User user = springSecurityService.currentUser
  springSecurityService.reauthenticate("harponen")
  
  //Class ssu = this.class.classLoader.loadClass("org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils")
  //ssu.getMethod("reloadSecurityConfig").invoke(new Object[0])
  debug(springSecurityService.currentUser.toString())
  
  //sql = groovy.sql.Sql.newInstance("jdbc:mysql://192.168.10.20:3306/hft", 'root', 'admin', "com.mysql.jdbc.Driver")
  com.unifina.security.User.list().each {debug("$it.username $it.password")}
  
  Thread t = new Thread("illegal")
  t.start()
	 */
	
    void testFile() {
		Class c = cl.parseClass("class CustomModule implements Runnable { void run() {new File('C:\\restricted.txt').eachLine {println(it)}} }", "TestFile.groovy")
		assert c != null
		shouldFail {
			c.newInstance().run()
		}
    }
	
	void testExit() {
		Class c = cl.parseClass("class CustomModule implements Runnable { void run() {System.exit(1)} }", "TestFile.groovy")
		assert c != null
		shouldFail {
			c.newInstance().run()
		}
	}
	
	void testParseClass() {
		Class c = cl.parseClass("class CustomModule implements Runnable { void run() {this.class.classLoader.parseClass('class Foo { }','Foo').newInstance()} }", "TestFile.groovy")
		assert c != null
		shouldFail {
			c.newInstance().run()
		}
	}
	
	void testPackageRestriction() {
		Class c = cl.parseClass("class CustomModule implements Runnable { void run() {this.class.classLoader.loadClass('java.net.URLClassLoader')} }", "TestFile.groovy")
		assert c != null
		shouldFail {
			c.newInstance().run()
		}
	}
	
	void testGetClassLoader() {
		Class c = cl.parseClass("class CustomModule { void run(Class someClass) {def cl = someClass.classLoader; cl.loadClass('java.net.URLClassLoader')} }", "TestFile.groovy")
		def someClass = this.class
		assert c != null
		shouldFail {
			c.newInstance().run(someClass)
		}
	}
}
