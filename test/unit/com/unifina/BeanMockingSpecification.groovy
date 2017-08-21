package com.unifina

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import grails.util.Holders
import spock.lang.Specification

/**
 * Insert any instance you like as a spring bean by using mockBean(name, instance) in your setup() method.
 */
@TestMixin(GrailsUnitTestMixin)
class BeanMockingSpecification extends Specification {

	private registeredMockBeans = []

	/**
	 * Slight hack: setup() and cleanup() can't be overridden, but it's great to have
	 * cleanupMockBeans called automatically. So subclasses should override doCleanup() instead.
     */
	def cleanup() {
		doCleanup()
		cleanupMockBeans()
	}

	/**
	 * Override this instead of cleanup() if you need it
	 */
	def doCleanup() {}

	protected <T> T getBean(Class<T> clazz) {
		Holders.getApplicationContext().getBean(clazz)
	}

	protected <T> T mockBean(Class<T> clazz, T instance) {
		String name = clazz.getName().substring(0,1).toLowerCase() + clazz.getName().substring(1)
		Holders.getApplicationContext().beanFactory.registerSingleton(name, instance)
		registeredMockBeans << name
		return instance
	}

	protected void cleanupMockBeans() {
		registeredMockBeans.each {
			Holders.getApplicationContext().beanFactory.destroySingleton(it);
		}
		registeredMockBeans.clear()
	}

}
