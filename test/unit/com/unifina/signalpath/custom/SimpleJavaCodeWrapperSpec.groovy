package com.unifina.signalpath.custom

import com.unifina.service.SerializationService
import com.unifina.utils.Globals
import com.unifina.utils.GlobalsFactory
import com.unifina.utils.testutils.ModuleTestHelper
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification

@TestMixin(GrailsUnitTestMixin)
class SimpleJavaCodeWrapperSpec extends Specification {

	SimpleJavaCodeWrapper module
	Globals globals
	
    def setup() {
		defineBeans {
			serializationService(SerializationService)
		}

		module = new SimpleJavaCodeWrapper()
		globals = module.globals = GlobalsFactory.createInstance([:], grailsApplication)
		module.init()
		module.configure([
		    code: "\n" +
				"TimeSeriesInput in = new TimeSeriesInput(this,\"in\");\n" +
				"TimeSeriesOutput out = new TimeSeriesOutput(this,\"out\");\n" +
				"private double sum = 0D;\n" +
				"\n" +
				"@Override\n" +
				"public void sendOutput() {\n" +
					"sum += in.value;\n" +
					"out.send(sum);\n" +
				"}\n" +
				"\n" +
				"@Override\n" +
				"public void clearState() {\n" +
				"sum = 0D;\n" +
				"}\n"
		])
    }

	void "simpleJavaCodeWrapper gives correct answer"() {
		when:
		Map inputValues = [
			in: [0,1,2,3,4,5,6,7,8,9,10].collect {it?.doubleValue()},
		]
		Map outputValues = [
			out: [0,1,3,6,10,15,21,28,36,45,55].collect {it?.doubleValue()},
		]
		
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues)
			.overrideGlobals { globals }
			.test()
	}
}
