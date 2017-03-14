package com.unifina.signalpath

import com.unifina.signalpath.filtering.ExponentialMovingAverage
import com.unifina.signalpath.filtering.MovingAverageModule
import com.unifina.signalpath.simplemath.Count
import com.unifina.signalpath.simplemath.Divide
import com.unifina.signalpath.simplemath.Multiply
import com.unifina.signalpath.simplemath.Subtract
import com.unifina.signalpath.statistics.StandardDeviation
import com.unifina.signalpath.utils.Label
import groovy.transform.CompileStatic
import spock.lang.Specification

import static com.unifina.signalpath.PropagatorUtils.*

class PropagatorUtilsSpec extends Specification {

	Divide divideModule
	Multiply multiplyModule
	Subtract subtractModule
	ExponentialMovingAverage exponentialMovingAverageModule
	StandardDeviation standardDeviationModule
	MovingAverageModule movingAverageModule
	Count countModule
	Label labelModule

	Set<AbstractSignalPathModule> allModules

	@CompileStatic
	private List<Output> createModuleGraph(boolean createFirstLevel) {
		divideModule = new Divide()
		multiplyModule = new Multiply()
		subtractModule = new Subtract()
		exponentialMovingAverageModule = new ExponentialMovingAverage()
		standardDeviationModule = new StandardDeviation()
		movingAverageModule = new MovingAverageModule()
		countModule = new Count()
		labelModule = new Label()

		allModules = [
			divideModule,
			multiplyModule,
			subtractModule,
			exponentialMovingAverageModule,
			standardDeviationModule,
			movingAverageModule,
			countModule,
			labelModule,
		] as Set

		// Naming modules helps read assertion failure messages
		allModules.each { AbstractSignalPathModule m -> m.name = m.class.simpleName }

		// Init modules to ensure endpoint availability
		allModules*.init()

		def rootOutput1 = new Output(null, "out1", null)
		def rootOutput2 = new Output(null, "out2", null)

		// 1st level of DAG
		if (createFirstLevel) {
			rootOutput1.connect(divideModule.getInput("A"))
			rootOutput1.connect(multiplyModule.getInput("A"))
			rootOutput2.connect(subtractModule.getInput("A"))
		}

		// 2nd level of DAG
		divideModule.getOutput("A/B").connect(exponentialMovingAverageModule.getInput("in"))
		divideModule.getOutput("A/B").connect(standardDeviationModule.getInput("in"))
		multiplyModule.getOutput("A*B").connect(movingAverageModule.getInput("in"))

		// 3rd level of DAG
		exponentialMovingAverageModule.getOutput("out").connect(countModule.getInput("windowLength"))
		movingAverageModule.getOutput("out").connect(countModule.getInput("in"))

		// 4th level of DAG
		countModule.getOutput("count").connect(labelModule.getInput("label"))

		return [rootOutput1, rootOutput2]
	}

	def "findReachableSet returns no modules given empty list"() {
		expect:
		findReachableSetFromOutputs([]).empty
	}

	def "findReachableSet returns no modules given outputs not connected to anything"() {
		def outputs = [new Output<>(null, "out1", null), new Output<>(null, "out2", null)]
		expect:
		findReachableSetFromOutputs(outputs).empty
	}

	def "findReachableSet finds all modules if connected"() {
		def outputs = createModuleGraph(true)
		expect:
		findReachableSetFromOutputs(outputs) == allModules
	}

	def "findReachableSet does not find unreachable modules"() {
		def outputs = createModuleGraph(true)
		def allModulesExceptSubtract = allModules
		allModulesExceptSubtract.remove(subtractModule)

		expect:
		findReachableSetFromOutputs([outputs[0]]) == allModulesExceptSubtract
	}

	def "findReachableSet does not find unreachable modules #2"() {
		def outputs = createModuleGraph(true)
		exponentialMovingAverageModule.getOutput("out").disconnect()
		movingAverageModule.getOutput("out").disconnect()

		def allModulesExceptCountAndLabel = allModules
		allModulesExceptCountAndLabel.removeAll([countModule, labelModule])

		expect:
		findReachableSetFromOutputs(outputs) == allModulesExceptCountAndLabel
	}

	def "findDependentModules returns no modules given empty arguments"() {
		expect:
		findDependentModules([] as Set, [] as Set).empty
	}

	def "findDependentModules returns no modules given empty sources"() {
		createModuleGraph(false)
		expect:
		findDependentModules(allModules, [] as Set).empty
	}

	def "findDependentModules returns no modules given label, subtract, and standardDeviation"() {
		createModuleGraph(false)
		def sources = [labelModule, subtractModule, standardDeviationModule] as Set
		expect:
		findDependentModules(allModules, sources).empty
	}

	def "findDependentModules returns label given count"() {
		createModuleGraph(false)
		def sources = [countModule] as Set
		expect:
		findDependentModules(allModules, sources) == [labelModule] as Set
	}

	def "findDependentModules returns count, label given exponentialMovingAverage"() {
		createModuleGraph(false)
		def sources = [exponentialMovingAverageModule] as Set
		expect:
		findDependentModules(allModules, sources) == [countModule, labelModule] as Set
	}

	def "findDependentModules returns exponentialMovingAverage, standardDeviation, movingAverage, count, label given multiply, divide"() {
		createModuleGraph(false)
		def sources = [multiplyModule, divideModule] as Set
		expect:
		findDependentModules(allModules, sources) == [
			exponentialMovingAverageModule, standardDeviationModule, movingAverageModule, countModule, labelModule
		] as Set
	}
}
