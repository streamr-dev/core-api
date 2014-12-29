package com.unifina.signalpath.messaging

import grails.test.mixin.*
import grails.test.mixin.support.GrailsUnitTestMixin

import org.codehaus.groovy.grails.commons.GrailsApplication

import spock.lang.Specification

import com.unifina.datasource.RealtimeDataSource
import com.unifina.domain.security.SecUser
import com.unifina.push.PushChannel
import com.unifina.signalpath.NotificationMessage
import com.unifina.signalpath.SignalPath
import com.unifina.utils.Globals

@TestMixin(GrailsUnitTestMixin)
public class EmailModuleSpec extends Specification {
	
	String timeStamp
	EmailModule module
	MockMailService ms

	Globals globals
	boolean notificationSent = false

	void setup() {
		
		defineBeans {
			mailService(MockMailService)
		}
		ms = grailsApplication.mainContext.getBean("mailService")
		assert ms != null
		
		grailsApplication.config.unifina.email.sender = "sender"
		
		
		globals = new Globals([:], grailsApplication, new SecUser(timezone:"Europe/Helsinki", username: "username"))
		globals.time = new Date()
				
		module = new EmailModule()
		module.globals = globals
		module.init()
		module.inputCount = 2	
		module.configure(module.getConfiguration())
		
	}

	void "module should send an email for a realtime datasource"(){
		globals.dataSource = new RealtimeDataSource()
		
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
			ms.to == globals.getUser().getUsername()
		then: "subject must be correct"
			ms.subject == "Test subject"
		then: "body must be correct"
			ms.body == """
Message:
Test message

Event Timestamp:
${module.df.format(globals.time)}

Input Values:
value1: 500
value2: test value

"""
	}
	
	void "module should send a notification for a non-realtime datasource"(){
		module.parentSignalPath = new SignalPath()
		globals.uiChannel = Mock(PushChannel)
		
		when:
			module.sub.receive("Test subject")
			module.message.receive("Test message")
			module.getInput("value1").receive(500)
			module.getInput("value2").receive("test value")
			module.sendOutput()
		then: "email must not be sent"
			!ms.mailSent
		then: "notification must be sent"
			1 * globals.uiChannel.push(new NotificationMessage("""
Message:
Test message

Event Timestamp:
${module.df.format(globals.time)}

Input Values:
value1: 500
value2: test value

"""), module.parentSignalPath.uiChannelId)
	}
	
	void "If trying to send emails too often send notification to warn about it"(){
		globals.dataSource = new RealtimeDataSource()
		module = new EmailModule(){
			long myTime
			
			public long getTime() {
				return myTime
			}
			
			public void setTime(long time) {
				myTime = time
			}
		}
		module.parentSignalPath = new SignalPath()
		globals.uiChannel = Mock(PushChannel)
		module.globals = globals
		module.init()
		module.inputCount = 2	
		module.configure(module.getConfiguration())
	
		when: "sent two emails very often"
			module.setTime(0)
			module.sub.receive("Test subject")
			module.message.receive("Test message")
			module.getInput("value1").receive(500)
			module.getInput("value2").receive("test value")
			module.sendOutput()
			module.setTime(100)
			module.sendOutput()
		then: "one notification should be sent"
			!ms.mailSent	
			1 * globals.uiChannel.push(new NotificationMessage("Tried to send emails too often"), module.parentSignalPath.uiChannelId)
			
		when: "sent third email after one minute"
			module.setTime(70000)
			module.sub.receive("Test subject")
			module.message.receive("Test message")
			module.getInput("value1").receive(500)
			module.getInput("value2").receive("test value")
			module.sendOutput()
		then: "an email should be sent again"
			ms.mailSent
			0 * globals.uiChannel.push(new NotificationMessage("Tried to send emails too often"), module.parentSignalPath.uiChannelId)
	}


}


