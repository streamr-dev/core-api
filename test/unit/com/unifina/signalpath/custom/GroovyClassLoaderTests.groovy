package com.unifina.signalpath.custom

import static org.junit.Assert.*
import grails.test.mixin.*
import grails.test.mixin.support.*
import groovy.transform.CompileStatic

import org.junit.*

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
class GroovyClassLoaderTests {

    void setUp() {
        // Setup logic here
    }

    void tearDown() {
        // Tear down logic here
    }

//	@CompileStatic
//    void testMemoryLeak() {
//        String newClass = "class CLASSNAME {}"
//		for (int i=0;i<10000000;i++) {
//			GroovyClassLoader gcl = new GroovyClassLoader()
//			Class clazz = gcl.parseClass(newClass.replace("CLASSNAME", "NewClass"+System.nanoTime()))
//			clazz.newInstance()
//		}
//    }
}
