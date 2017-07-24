package com.unifina.service

import com.unifina.signalpath.AbstractSignalPathModule
import com.unifina.signalpath.SignalPath
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.AutowireCapableBeanFactory

class DependencyInjectionService {

	@Autowired
	AutowireCapableBeanFactory beanFactory

	@CompileStatic
	void autowire(AbstractSignalPathModule module) {
		beanFactory.autowireBean(module)
		if (module instanceof SignalPath) {
			((SignalPath) module).modules.each { AbstractSignalPathModule m -> autowire(m) }
		}
	}
}
