package com.unifina.signalpath.messaging

import com.unifina.UiChannelMockingSpecification
import com.unifina.datasource.RealtimeDataSource
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Canvas
import com.unifina.signalpath.NotificationMessage
import com.unifina.signalpath.SignalPath
import com.unifina.utils.Globals
import com.unifina.utils.GlobalsFactory
import com.unifina.utils.testutils.ModuleTestHelper
import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import grails.test.runtime.FreshRuntime
import org.codehaus.groovy.grails.commons.InstanceFactoryBean

import java.text.SimpleDateFormat

@FreshRuntime
@TestMixin(GrailsUnitTestMixin)
@Mock(SecUser)
class EmailModuleSpec extends UiChannelMockingSpecification {

	EmailModule module
	MockMailService ms
	MockCanvasService cs

	Globals globals

	def doWithSpring = {
		springSecurityService(InstanceFactoryBean, null, SpringSecurityService)
	}

	void setup() {
		mockServicesForUiChannels()

		defineBeans {
			mailService(MockMailService)
			canvasService(MockCanvasService)
		}
		ms = grailsApplication.mainContext.getBean("mailService")
		assert ms != null
		cs = grailsApplication.mainContext.getBean("canvasService")
		assert cs != null

		grailsApplication.config.unifina.email.sender = "sender"
		
		module = new EmailModule()
		module.canvasService = cs
	}

	private void initContext(Map context = [:], SecUser user = new SecUser(timezone:"Europe/Helsinki", username: "username").save(failOnError: true, validate: false)) {
		globals = GlobalsFactory.createInstance(context, user)
		globals.time = new Date()

		module.globals = globals
		module.init()
		module.emailInputCount = 2
		module.configure(module.getConfiguration())

		module.parentSignalPath = new SignalPath(true)
		module.parentSignalPath.globals = globals
		module.parentSignalPath.configure([uiChannel: [id: "uiChannel"]])

		module.rootSignalPath.canvas = new Canvas()
		module.rootSignalPath.canvas.save()
	}

	void "emailModule sends the correct email"() {
		initContext()
		globals.realtime = true
		globals.dataSource = new RealtimeDataSource()
		when:
		Map inputValues = [
			subject: ["Subject"],
			message: ["Message"],
			value1: [1],
			value2: ["abcd"],
		]
		Map outputValues = [:]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues)
			.overrideGlobals {
				globals.time = new Date(0)
				globals
			}
			.afterEachTestCase {
				assert ms.mailSent
				assert ms.from == grailsApplication.config.unifina.email.sender
				assert ms.to == "username"
				assert ms.subject == "Subject"
				assert ms.body == """
This email was sent by one of your running Canvases on Streamr.

Message:
Message

Event Timestamp:
1970-01-01 02:00:00.000

Input Values:
value1: 1
value2: abcd

To view, edit, or stop the Canvas that sent this message, click the below link:
https://www.streamr.com/canvas/editor/1
"""
				ms.clear()
			}
			.test()
	}

	void "module should send an email for a realtime datasource"() {
		initContext()
		globals.realtime = true
		def df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
		df.setTimeZone(TimeZone.getTimeZone("Europe/Helsinki"))

		when: "feedback sent from the feedback page"
			module.sub.receive("Test subject")
			module.message.receive("Test message")
			module.getInput("value1").receive(500)
			module.getInput("value2").receive("test value")
			module.sendOutput()
		then: "email should be sent"
			ms.mailSent
		then: "sender is correct"
			ms.from == grailsApplication.config.unifina.email.sender
		then: "receiver must be correct"
			ms.to == "username"
		then: "subject must be correct"
			ms.subject == "Test subject"
		then: "body must be correct"
			ms.body == """
This email was sent by one of your running Canvases on Streamr.

Message:
Test message

Event Timestamp:
${df.format(globals.time)}

Input Values:
value1: 500
value2: test value

To view, edit, or stop the Canvas that sent this message, click the below link:
https://www.streamr.com/canvas/editor/1
"""
	}

	void "module should send a notification for a non-realtime datasource"() {
		initContext()
		globals.realtime = false

		when:
			module.sub.receive("Test subject")
			module.message.receive("Test message")
			module.getInput("value1").receive(500)
			module.getInput("value2").receive("test value")
			module.sendOutput()
		then: "email must not be sent"
			!ms.mailSent
		then: "notification must be sent"
			getSentMessagesByStreamId()[module.parentSignalPath.getUiChannel().getId()].size() == 1
			getSentMessagesByStreamId()[module.parentSignalPath.getUiChannel().getId()][0] instanceof NotificationMessage
	}

	void "If trying to send emails too often send notification to warn about it"() {
		setup:
			initContext()
			globals.realtime = true

		when: "sent two emails very often"
			globals.time = new Date(0)
			module.sub.receive("Test subject")
			module.message.receive("Test message")
			module.getInput("value1").receive(500)
			module.getInput("value2").receive("test value")
			module.sendOutput()
			ms.mailSent = false // Clear
			globals.time = new Date(10000)
			module.sendOutput()
		then: "one notification should be sent"
			!ms.mailSent
			getSentMessagesByStreamId()[module.parentSignalPath.getUiChannel().getId()].size() == 1
			getSentMessagesByStreamId()[module.parentSignalPath.getUiChannel().getId()][0] instanceof NotificationMessage

		when: "sent third email with a warning after one minute"
			globals.time = new Date(70000)
			module.sub.receive("Test subject")
			module.message.receive("Test message")
			module.getInput("value1").receive(500)
			module.getInput("value2").receive("test value")
			module.sendOutput()
		then: "an email should be sent again, but no new notification is shown"
			ms.mailSent
			getSentMessagesByStreamId()[module.parentSignalPath.getUiChannel().getId()].size() == 1
	}

	void "if too emails are sent too frequently, the next one contains a warning about it"() {
		setup: "two emails sent frequently"
			initContext()
			globals.realtime = true

			globals.time = new Date(0)
			module.sub.receive("Test subject")
			module.message.receive("Test message")
			module.getInput("value1").receive(500)
			module.getInput("value2").receive("test value")
			module.sendOutput()
			globals.time = new Date(30000)
			module.sendOutput()

		when: "third one sent"
			globals.time = new Date(70000)
			module.sendOutput()

		then: "email sent and has warning in it"
			ms.mailSent
			ms.getBody().contains("WARNING")

		when: "fourth one sent"
			ms.clear()
			globals.time = new Date(150000)
			module.sendOutput()

		then: "email sent but hasn't warning in it anymore"
			ms.mailSent
			!ms.getBody().isEmpty()
			!ms.getBody().contains("WARNING")
	}

	void "EmailModule can be instantiated without a user"() {
		expect:
			initContext([:], null)
	}

}


