package com.unifina.signalpath.simplemath

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class ExpressionSpec extends Specification {

	Expression module

	def setup() {
		module = new Expression()
		module.init()
	}

	void "Invalid expression causes error on onConfiguration()"() {
		module.getInput("expression").receive("5++^a")

		when:
		module.configure(module.getConfiguration())

		then:
		com.udojava.evalex.Expression.ExpressionException e = thrown()
		e.getMessage() == "Unknown operator '++^' at position 2"
	}

	void "Expression gives the right answer"() {
		module.getInput("expression").receive("COS(DEG(x)) + SIN(DEG(y)) + IF (flag == 1, LOG(z), 0)")
		module.configure(module.getConfiguration())

		when:
		Map inputValues = [
			x:      [Math.PI, Math.PI,  0, 0]*.doubleValue(),
			y:      [Math.PI, Math.PI,  0, 0]*.doubleValue(),
			z: 		[1,   Math.exp(5),  1, -5]*.doubleValue(),
			flag:   [3,             1,  0, 1]*.doubleValue(),
		]
		Map outputValues = [
			out:   [-1, 4, 1, 1]*.doubleValue(),
			error: [null,  null, null, "Infinite or NaN"]
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}

	void "CORE-849 Variables used many times only generate one output"() {
		when:
		module.getInput("expression").receive("x*x+y*y")
		module.configure(module.getConfiguration())

		then:
		module.inputs.size() == 3	// expression, x, y
	}

	void "CORE-883 can handle large numbers"() {
		when:
		module.getInput("expression").receive("x + y")
		module.configure(module.configuration)

		module.getInput("x").receive(1490357362484)
		module.getInput("y").receive(360000)
		module.trySendOutput()

		then:
		module.getOutput("out").getValue() == 1490357362484d + 360000d
	}
}
