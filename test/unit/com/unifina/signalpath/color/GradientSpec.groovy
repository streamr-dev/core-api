package com.unifina.signalpath.color

import com.unifina.utils.StreamrColor
import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class GradientSpec extends Specification {
	
	Gradient module
	
    def setup() {
		module = new Gradient()
		module.init()
    }

	// Color converter used to create test data: (19.02.2016)
	// http://www.workwithcolor.com/color-converter-01.htm

	void "gradient works correctly with scale 0 to 1 and white to black (hue 0)"() {
		setup:
		module.configure([
			params: [
					[name: "minValue", value: 0],
					[name: "maxValue", value: 1],
					[name: "minColor", value: new StreamrColor(255,255,255)],
					[name: "maxColor", value: new StreamrColor(0,0,0)]
			]
		])
		when:
		Map inputValues = [
			in: [0, 0.2, 0.4, 0.6, 0.8, 1.0].collect {it?.doubleValue()},
		]
		Map outputValues = [
				color:[
				        new StreamrColor(255, 255, 255),
				        new StreamrColor(204, 204, 204),
				        new StreamrColor(153, 153, 153),
				        new StreamrColor(102, 102, 102),
				        new StreamrColor(51, 51, 51),
				        new StreamrColor(0, 0, 0),
				]
		]
		
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues)
			.outputValues(outputValues)
			.test()
	}

	void "gradient works correctly with scale 0 to 1 and green to red (s = b = 1)"() {
		setup:
		module.configure([
			params: [
					[name: "minValue", value: 0],
					[name: "maxValue", value: 1],
					[name: "minColor", value: new StreamrColor(0,255,0)],
					[name: "maxColor", value: new StreamrColor(255,0,0)]
			]
		])
		when:
		Map inputValues = [
			in: [0, 0.2, 0.4, 0.6, 0.8, 1.0].collect {it?.doubleValue()},
		]
		Map outputValues = [
			color:[
					new StreamrColor(0,255,0),
					new StreamrColor(102, 255, 0),
					new StreamrColor(204, 255, 0),
					new StreamrColor(255, 204, 0),
					new StreamrColor(255,102,0),
					new StreamrColor(255,0,0)
			]
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues)
			.outputValues(outputValues)
			.test()
	}

	void "gradient works correctly with scale 0 to 1 and more complex colors"() {
		setup:
		module.configure([
				params: [
						[name: "minValue", value: 0],
						[name: "maxValue", value: 1],
						[name: "minColor", value: new StreamrColor(0, 128, 0)],
						[name: "maxColor", value: new StreamrColor(204, 95, 41)]
				]
		])
		when:
		Map inputValues = [
				in: [0, 0.2, 0.4, 0.6, 0.8, 1.0].collect {it?.doubleValue()},
		]
		Map outputValues = [
				color:[
						new StreamrColor(0, 128, 0),
						// Converter gave (51, 143, 6)
						new StreamrColor(52, 143, 6),
						new StreamrColor(110, 158, 13),
						// Converter gave (173, 173, 21)
						new StreamrColor(174, 173, 21),
						new StreamrColor(189, 136, 30),
						new StreamrColor(204, 95, 41)
				]
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues)
				.outputValues(outputValues)
				.test()
	}

	void "gradient works correctly with scale 1 to 0 (min bigger than max) and more complex colors"() {
		setup:
		module.configure([
				params: [
						[name: "minValue", value: 1],
						[name: "maxValue", value: 0],
						[name: "minColor", value: new StreamrColor(0, 128, 0)],
						[name: "maxColor", value: new StreamrColor(204, 95, 41)]
				]
		])
		when:
		Map inputValues = [
				in: [1.0, 0.8, 0.6, 0.4, 0.2, 0.0].collect {it?.doubleValue()},
		]
		Map outputValues = [
				color:[
						new StreamrColor(0, 128, 0),
						// Converter gave (51, 143, 6)
						new StreamrColor(52, 143, 6),
						new StreamrColor(110, 158, 13),
						// Converter gave (173, 173, 21)
						new StreamrColor(174, 173, 21),
						new StreamrColor(189, 136, 30),
						new StreamrColor(204, 95, 41)
				]
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues)
				.outputValues(outputValues)
				.test()
	}

	void "gradient calculates hue going the shortest way (0.9 -> 1.0 -> 0.1)"() {
		setup:
		module.configure([
				params: [
						[name: "minValue", value: 0],
						[name: "maxValue", value: 1],
						[name: "minColor", value: new StreamrColor(255, 0, 255)],
						[name: "maxColor", value: new StreamrColor(255, 255, 0)]
				]
		])
		when:
		Map inputValues = [
				in: [0, 0.2, 0.4, 0.6, 0.8, 1.0].collect {it?.doubleValue()},
		]
		Map outputValues = [
				color:[
						new StreamrColor(255, 0, 255),
						new StreamrColor(255, 0, 153),
						new StreamrColor(255, 0, 51),
						new StreamrColor(255, 51, 0),
						new StreamrColor(255, 153, 0),
						new StreamrColor(255, 255, 0)
				]
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues)
				.outputValues(outputValues)
				.test()
	}

	void "gradient gives the extreme values when input is bigger than max / smaller than min"() {
		setup:
		module.configure([
				params: [
						[name: "minValue", value: 1],
						[name: "maxValue", value: 0],
						[name: "minColor", value: new StreamrColor(0, 0, 0)],
						[name: "maxColor", value: new StreamrColor(255, 255, 255)]
				]
		])
		when:
		Map inputValues = [
				in: [-1, -0.5, 0, 1, 2, 3].collect {it?.doubleValue()},
		]
		Map outputValues = [
				color:[
						new StreamrColor(255, 255, 255),
						new StreamrColor(255, 255, 255),
						new StreamrColor(255, 255, 255),
						new StreamrColor(0, 0, 0),
						new StreamrColor(0, 0, 0),
						new StreamrColor(0, 0, 0)
				]
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues)
				.outputValues(outputValues)
				.test()
	}
}
