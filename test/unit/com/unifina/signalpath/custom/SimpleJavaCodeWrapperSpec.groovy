package com.unifina.signalpath.custom

import com.unifina.service.SerializationService
import com.unifina.signalpath.ModuleException
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
		module.hash = 666
		module.configure(module.getConfiguration())
		module.connectionsReady()
    }

	void "simpleJavaCodeWrapper gives correct answer"() {
		setup:
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

	void "it throws ModuleException if given non-compiling code"() {
		when:
		module.configure([
			code: "\nklgagj98989832[]}{}{}"
		])

		then:
		thrown(ModuleException)
	}

	void "it throws ModuleException if shadowing variables"() {
		when:
		module.configure([
			code: "\n" +
				"TimeSeriesInput in = new TimeSeriesInput(this,\"in\");\n" +
				"TimeSeriesOutput out = new TimeSeriesOutput(this,\"out\");\n" +
				"public double readyInputs = 0D;\n" +
				"@Override\n" +
				"public void sendOutput() {}\n" +
				"@Override\n" +
				"public void clearState() {}\n"
		])

		then:
		ModuleException ex = thrown()
		ex.message.contains("'readyInputs'")
	}

	void "it throws ModuleException if field contains reference to anonymous inner class"() {
		when:
		module.configure([
			code: "\n" +
				"TimeSeriesInput in = new TimeSeriesInput(this,\"in\") {};\n" +
				"TimeSeriesOutput out = new TimeSeriesOutput(this,\"out\");\n" +
				"@Override\n" +
				"public void sendOutput() {}\n" +
				"@Override\n" +
				"public void clearState() {}\n"
		])

		then:
		ModuleException ex = thrown()
		ex.message == "Anonymous inner classes are not allowed."
	}
}
