package com.unifina

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import grails.util.Holders
import spock.lang.Specification

/**
 * Insert any instance you like as a spring bean, use mockBean(name, instance) in your setup() method.
 * Then in cleanup(), call cleanupMockBeans().
 */
@TestMixin(GrailsUnitTestMixin)
class BeanMockingSpec extends Specification {

	private registeredMockBeans = []

	protected void getBean(Class clazz) {
		Holders.getApplicationContext().getBean(clazz)
	}

	protected void mockBean(Class clazz, Object instance) {
		String name = clazz.getName().substring(0,1).toLowerCase() + clazz.getName().substring(1)
		Holders.getApplicationContext().beanFactory.registerSingleton(name, instance)
		registeredMockBeans << name
	}

	protected void cleanupMockBeans() {
		registeredMockBeans.each {
			Holders.getApplicationContext().beanFactory.destroySingleton(it);
		}
		registeredMockBeans.clear()
	}

}
