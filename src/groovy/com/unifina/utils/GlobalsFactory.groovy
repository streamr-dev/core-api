package com.unifina.utils

import org.apache.log4j.Logger
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.springframework.beans.factory.NoSuchBeanDefinitionException

import com.unifina.security.SecUser

class GlobalsFactory {
	
	private static final Logger log = Logger.getLogger(GlobalsFactory.class)
	
	public static Globals createInstance(Map signalPathContext, GrailsApplication grailsApplication) {
		return new GlobalsFactory().create(signalPathContext, grailsApplication)
	}
	
	public Globals create(Map signalPathContext, GrailsApplication grailsApplication) {
		Class clazz = this.getClass().getClassLoader().loadClass(grailsApplication?.config.unifina.globals.className ?: "com.unifina.utils.Globals")
		SecUser user = null
		try {
			user = grailsApplication.mainContext.getBean("springSecurityService").currentUser
		} catch (NoSuchBeanDefinitionException e) {
			log.warn("create: springSecurityService is not defined, current user is null!")
		}
		return clazz.newInstance(signalPathContext,grailsApplication,user)
	} 
}
