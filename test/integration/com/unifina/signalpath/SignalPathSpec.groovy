package com.unifina.signalpath

import com.unifina.domain.data.Stream
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
//@Mock([Stream, SecUserSecRole, ModulePackage, ModulePackageUser])
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

		def userId = makeUser()
		user = SecUser.get(userId)
		def streamId = createStream(user)
		stream = Stream.get(streamId)
    }

	void "and gives the right answer"() {
		when:

		def savedStructure = readSavedStructure(stream)
		def runningSignalPathId = createAndRun(savedStructure, user)

		Random r = new Random()

		for (int i = 0; i < 100; ++i) {
			kafkaService.sendMessage(stream, "", [a: i, b: i * 2.5, c: i % 3 == 0])
			if (servletContext["signalPathRunners"]) {
				servletContext["signalPathRunners"].iterator().next().value.signalPaths[0].mods.each {
					println it.outputs
				}
			}

			if (r.nextDouble() < 0.05) {
				sleep(3000)
				def rsp = RunningSignalPath.get(runningSignalPathId)
				signalPathService.stopLocal(rsp)
				signalPathService.startLocal(rsp, savedStructure.signalPathContext)
			}

			sleep(100)
		}

		sleep(2000)

		def actual = servletContext["signalPathRunners"].iterator().next().value.signalPaths[0].mods.collect {
			def h = [:]
			h[it.getName()] = it.outputs.collect { it.getValue() }
			h
		}
		signalPathService.stopLocal(RunningSignalPath.get(runningSignalPathId))
		then:
		actual == [[Stream:[99.0, 247.5, 1.0]], [Count:[100.0]], [Count:[100.0]], [Count:[100.0]], [Add:[300.0]]]
	}

	private def createStream(SecUser secUser) {
		def s = streamService.createUserStream([name: "serializationTestStream"], secUser)
		s.streamConfig = ([fields: [[name: "a", type: "number"], [name: "b", type: "number"], [name: "c", type: "boolean"]], topic: s.uuid] as JSON)
		s.save()
		s.id
	}

	private def makeUser() {
		def user = new SecUser(
			username: "serialization-test@streamr.com",
			password: "foo",
			name: "eric",
			timezone: "Europe/Helsinki")
		user.save(flush: true)
		user.id
	}

	private Object readSavedStructure(stream) {
		def s = new JsonSlurper().parseText(new File(getClass().getResource("signal-path-data.json").path).text)
		s.signalPathData.modules[0].params[0].value = stream.id
		s.signalPathContext.live = true
		s
	}

	private def createAndRun(savedStructure, SecUser user) {
		RunningSignalPath rsp = signalPathService.createRunningSignalPath(
			savedStructure["signalPathData"],
			user,
			false,
			true)

		signalPathService.startLocal(rsp, savedStructure["signalPathContext"])
		return rsp.id
	}
}
