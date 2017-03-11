package com.unifina

import grails.util.Holders
import spock.lang.Specification

/**
 * Insert any instance you like as a spring bean, use mockBean(name, instance) in your setup() method.
 * Then in cleanup(), call cleanupMockBeans().
 */
class BeanMockingSpec extends Specification {

	private registeredMockBeans = []

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
