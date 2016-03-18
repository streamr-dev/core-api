package com.unifina.signalpath

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification

import com.unifina.push.PushChannel
import com.unifina.utils.GlobalsFactory

@TestMixin(GrailsUnitTestMixin)
class ModuleWithUISpec extends Specification {
	ModuleWithUI module
	
	def setup() {
		module = new ModuleWithUI() {
			@Override
			void sendOutput() {
				
			}
			@Override
			void clearState() {
				
			}
			@Override
			String getWebcomponentName() {
				return "webcomponent-name"
			}
		}
		
	}

	def cleanup() {
		
	}
	
	def "getConfiguration() must add uiChannel key to config"() {
		module.globals = GlobalsFactory.createInstance([:], grailsApplication)
		module.setName("TestModule")
		module.init()
		module.onConfiguration([:])
		def config
		
		when:
			config = module.getConfiguration()
		then: 
			config.uiChannel != null
			config.uiChannel.id != null
			config.uiChannel.name == "TestModule"
			config.uiChannel.webcomponent == module.webcomponentName
	}
	
	def "onConfiguration must read uiChannel id"() {
		module.globals = GlobalsFactory.createInstance([:], grailsApplication)
		module.init()
		
		when:
			module.onConfiguration([uiChannel:[id:'uiChannel-id']])
		then:
			module.getUiChannelId() == 'uiChannel-id'
	}
	
	def "getConfiguration() must add webcomponent name to uiChannel config when running live"() {
		module.globals = GlobalsFactory.createInstance([live:true], grailsApplication)
		module.init()
		module.onConfiguration([:])
		def config
		
		when:
			config = module.getConfiguration()
		then:
			config.uiChannel.webcomponent == "webcomponent-name"
	}
	
	def "connectionsReady() must add the ui channel to the PushChannel in globals"() {
		module.globals = GlobalsFactory.createInstance([:], grailsApplication)
		module.init()
		module.onConfiguration([uiChannel:[id:'uiChannel-id']])
		module.globals.setUiChannel(Mock(PushChannel))
		
		when:
			module.connectionsReady()
		then:
			1 * module.globals.getUiChannel().addChannel('uiChannel-id')
	}
}
