package com.unifina.signalpath.custom

import com.unifina.domain.security.SecUser
import com.unifina.security.MyPolicy
import com.unifina.security.MySecurityManager
import com.unifina.security.PackageAccessHelper
import com.unifina.service.SerializationService
import com.unifina.signalpath.ModuleException
import com.unifina.utils.Globals
import com.unifina.utils.GlobalsFactory
import com.unifina.utils.testutils.ModuleTestHelper
import com.unifina.utils.testutils.TestHelperException
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification

import java.security.AccessControlException
import java.security.Policy
import java.security.Security

@TestMixin(GrailsUnitTestMixin)
class SimpleJavaCodeWrapperSpec extends Specification {

	SimpleJavaCodeWrapper module
	Globals globals

	static Policy originalPolicy
	static SecurityManager originalSecurityManager

	def setupSpec() {
		if (!System.securityManager) {
			originalPolicy = Policy.getPolicy()
			originalSecurityManager = System.securityManager
			Security.setProperty("package.access", PackageAccessHelper.getRestrictedPackages().join(","))
			Policy.setPolicy(new MyPolicy())
			System.securityManager = new MySecurityManager()
		}
	}

	def cleanupSpec() {
		Policy.setPolicy(originalPolicy)
		System.securityManager = originalSecurityManager
	}
	
    def setup() {
		defineBeans {
			serializationService(SerializationService)
		}

		module = new SimpleJavaCodeWrapper()
		globals = module.globals = module.globals = GlobalsFactory.createInstance([:], new SecUser())
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

	void "it throws AccessDeniedException if trying to get data source"() {
		setup:
		module.configure([
			code: "\n" +
				"TimeSeriesInput in = new TimeSeriesInput(this,\"in\");\n" +
				"TimeSeriesOutput out = new TimeSeriesOutput(this,\"out\");\n" +
				"private double sum = 0D;\n" +
				"\n" +
				"@Override\n" +
				"public void sendOutput() {\n" +
				"Object user = getGlobals().getDataSource();\n" +
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

		new ModuleTestHelper.Builder(module, inputValues, outputValues)
			.overrideGlobals { globals }
			.test()
		then:
		def e = thrown(TestHelperException)
		e.cause.getClass() == AccessControlException
		e.cause.getMessage().contains("DataSource")
	}

	void "it throws AccessDeniedException if trying to set data source"() {
		setup:
		module.configure([
			code: "\n" +
				"TimeSeriesInput in = new TimeSeriesInput(this,\"in\");\n" +
				"TimeSeriesOutput out = new TimeSeriesOutput(this,\"out\");\n" +
				"private double sum = 0D;\n" +
				"\n" +
				"@Override\n" +
				"public void sendOutput() {\n" +
				"getGlobals().setDataSource(null);\n" +
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

		new ModuleTestHelper.Builder(module, inputValues, outputValues)
			.overrideGlobals { globals }
			.test()
		then:
		def e = thrown(TestHelperException)
		e.cause.getClass() == AccessControlException
		e.cause.getMessage().contains("DataSource")
	}

	void "it throws AccessDeniedException if trying to get source of input"() {
		setup:
		module.configure([
			code: "\n" +
				"TimeSeriesInput in = new TimeSeriesInput(this,\"in\");\n" +
				"TimeSeriesOutput out = new TimeSeriesOutput(this,\"out\");\n" +
				"private double sum = 0D;\n" +
				"\n" +
				"@Override\n" +
				"public void sendOutput() {\n" +
				"Object user = in.getSource();\n" +
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

		new ModuleTestHelper.Builder(module, inputValues, outputValues)
			.overrideGlobals { globals }
			.test()
		then:
		def e = thrown(TestHelperException)
		e.cause.getClass() == AccessControlException
		e.cause.getMessage().contains("ConnectionTraversalPermission")
	}

	void "it throws AccessDeniedException if trying to get targets of output"() {
		setup:
		module.configure([
			code: "\n" +
				"TimeSeriesInput in = new TimeSeriesInput(this,\"in\");\n" +
				"TimeSeriesOutput out = new TimeSeriesOutput(this,\"out\");\n" +
				"private double sum = 0D;\n" +
				"\n" +
				"@Override\n" +
				"public void sendOutput() {\n" +
				"Object user = out.getTargets();\n" +
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

		new ModuleTestHelper.Builder(module, inputValues, outputValues)
			.overrideGlobals { globals }
			.test()
		then:
		def e = thrown(TestHelperException)
		e.cause.getClass() == AccessControlException
		e.cause.getMessage().contains("ConnectionTraversalPermission")
	}
}
