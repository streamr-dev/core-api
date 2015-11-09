package com.unifina.signalpath

import com.unifina.domain.data.Stream
import com.unifina.domain.security.SecRole
import com.unifina.domain.security.SecUser
import com.unifina.domain.security.SecUserSecRole
import com.unifina.domain.signalpath.ModulePackage
import com.unifina.domain.signalpath.ModulePackageUser
import com.unifina.domain.signalpath.RunningSignalPath
import com.unifina.service.SignalPathService
import grails.converters.JSON
import grails.test.mixin.Mock
import grails.test.mixin.TestMixin
import grails.test.mixin.web.ControllerUnitTestMixin
import groovy.json.JsonSlurper
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.mock.web.MockServletContext
import spock.lang.Specification

@TestMixin(ControllerUnitTestMixin)
@Mock([SecUser, Stream, SecRole, SecUserSecRole, ModulePackage, ModulePackageUser, RunningSignalPath])
class SignalPathSpec extends Specification {

	def user

	def kafkaService
	def streamService
	def stream
	SignalPathService signalPathService
	SignalPath module

    def setup() {

		ConfigurableListableBeanFactory beanFactory = ((ConfigurableApplicationContext) grailsApplication.mainContext).getBeanFactory();
		beanFactory.registerSingleton("servletContext", new MockServletContext())
		signalPathService.servletContext = grailsApplication.mainContext.getBean("servletContext")
		servletContext = grailsApplication.mainContext.getBean("servletContext")

		user = makeUser()
		stream = createStream(user)
    }

	private Stream createStream(SecUser secUser) {
		def s = streamService.createUserStream([name: "serializationTestStream"], secUser)
		s.streamConfig = ([fields: [[name: "a", type: "number"], [name: "b", type: "number"], [name: "c", type: "boolean"]], topic: s.uuid] as JSON)
		s.save()
		s
	}

	void "and gives the right answer"() {
		when:
		Random r = new Random()
		def savedStructure = readSavedStructure(stream)
		RunningSignalPath rsp = createAndRun(savedStructure, user)
		for (int i = 0; i < 100; ++i) {
			kafkaService.sendMessage(stream, "", [a: i, b: i * 2.5, c: i % 3 == 0])
			servletContext["signalPathRunners"].iterator().next().value.signalPaths[0].mods.each {
				println it.outputs
			}
			sleep(1500)
			if (i != 0 && i % 10 == 0) {
				signalPathService.stopLocal(rsp)
				sleep(500)
				signalPathService.startLocal(rsp, savedStructure["signalPathContext"])
				sleep(500)
			}
		}
		then:
		rsp != null
	}

	private SecUser makeUser() {
		def user = new SecUser(
			username: "serialization-test@streamr.com",
			password: "foo",
			name: "eric",
			timezone: "Europe/Helsinki")
		user.save(validate:true)
		user
	}

	private Object readSavedStructure(stream) {
		def s = new JsonSlurper().parseText(new File(getClass().getResource("signal-path-data.json").path).text)
		s.signalPathData.modules[0].params[0].value = stream.id
		s.signalPathContext.live = true
		s
	}

	private RunningSignalPath createAndRun(savedStructure, SecUser user) {
		RunningSignalPath rsp = signalPathService.createRunningSignalPath(
			savedStructure["signalPathData"],
			user,
			false,
			true)

		signalPathService.startLocal(rsp, savedStructure["signalPathContext"])
		return rsp
	}
}
