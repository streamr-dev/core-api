package com.unifina.signalpath.charts

import com.unifina.utils.StreamrColor
import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class MapModuleSpec extends Specification {
	MapModule module

	def setup() {
		module = new MapModule()
		module.init()
	}

	Map inputValues = [
		traceColor: [new StreamrColor(233, 87, 15), null, null, new StreamrColor(6, 6, 6)],
		id: ["id-1", "id-2", "id-1", "id-3"],
		latitude: [60.412, 59.666, 30.000D, 0D]*.doubleValue(),
		longitude: [24.079, 66.999, -21.758D, 1D]*.doubleValue(),
	]

	void "MapModule pushes correct data to uiChannel"() {
		module.configure([
			uiChannel: [id: "mapPointData"],
			options: [
				drawTrace: [value: true],
			]
		])

		Map outputValues = [:]
		Map channelMessages = [
			mapPointData: [
				[t: "p", id: "id-1", lat: 60.412D, lng: 24.079D, color: "rgb(233, 87, 15)"],
				[t: "p", id: "id-2", lat: 59.666D, lng: 66.999D, color: "rgb(233, 87, 15)"],
				[t: "p", id: "id-1", lat: 30.000D, lng: -21.758D, color: "rgb(233, 87, 15)"],
				[t: "p", id: "id-3", lat: 0D, lng: 1D, color: "rgb(6, 6, 6)"],
			]
		]

		expect:
		new ModuleTestHelper.Builder(module, inputValues, outputValues)
			.uiChannelMessages(channelMessages)
			.test()
	}

	void "MapModule pushes correct data to uiChannel (with customMarkerLabel=true)"() {
		module.configure([
			uiChannel: [id: "mapPointData"],
			options: [
				drawTrace: [value: true],
				markerLabel: [value: true]
			]
		])

		inputValues["label"] = ["label", null, "label2", "label3"]
		Map outputValues = [:]
		Map channelMessages = [
			mapPointData: [
				[t: "p", id: "id-1", label: "label", lat: 60.412D, lng: 24.079D, color: "rgb(233, 87, 15)"],
				[t: "p", id: "id-2", label: "label", lat: 59.666D, lng: 66.999D, color: "rgb(233, 87, 15)"],
				[t: "p", id: "id-1", label: "label2", lat: 30.000D, lng: -21.758D, color: "rgb(233, 87, 15)"],
				[t: "p", id: "id-3", label: "label3", lat: 0D, lng: 1D, color: "rgb(6, 6, 6)"],
			]
		]

		expect:
		new ModuleTestHelper.Builder(module, inputValues, outputValues)
			.uiChannelMessages(channelMessages)
			.test()
	}

	void "MapModule works correctly with expiring markers"() {
		module.configure([
			uiChannel: [id: "mapPointData"],
			options: [
				drawTrace: [value: true],
				expiringTimeInSecs: [value: 2]
			]
		])

		Map inputValues = [
			id: [
				"id-1",
				"id-2", "id-3", "id-4",
				"id-3", "id-5", "id-6",
				"id-6", "id-3",
				"id-7", "id-1"
			],
			latitude: [
				1,
				2, 3, 4,
				33, 5, 6,
				66, 333,
				7, 11
			]*.doubleValue(),
			longitude: [
			    0,
				0, 0, 0,
				0, 0, 0,
				0, 0,
				0, 0
			]*.doubleValue(),
		]
		Map<Integer, Date> ticks = [
			0: new Date(0),
		    1: new Date(1000),
			4: new Date(2000),
			7: new Date(3000),
			9: new Date(4000),
			11: new Date(5000),
			12: new Date(6000)
		]
		Map outputValues = [:]
		Map channelMessages = [
			mapPointData: [
				[t: "p", id: "id-1", lat: 1D, lng: 0D, color: "rgb(233, 87, 15)"],
				[t: "p", id: "id-2", lat: 2D, lng: 0D, color: "rgb(233, 87, 15)"],
				[t: "p", id: "id-3", lat: 3D, lng: 0D, color: "rgb(233, 87, 15)"],
				[t: "p", id: "id-4", lat: 4D, lng: 0D, color: "rgb(233, 87, 15)"],
				[t: "d", list: ["id-1"]],
				[t: "p", id: "id-3", lat: 33D, lng: 0D, color: "rgb(233, 87, 15)"],
				[t: "p", id: "id-5", lat: 5D, lng: 0D, color: "rgb(233, 87, 15)"],
				[t: "p", id: "id-6", lat: 6D, lng: 0D, color: "rgb(233, 87, 15)"],
				[t: "d", list: ["id-2", "id-4"]],
				[t: "p", id: "id-6", lat: 66D, lng: 0D, color: "rgb(233, 87, 15)"],
				[t: "p", id: "id-3", lat: 333D, lng: 0D, color: "rgb(233, 87, 15)"],
				[t: "d", list: ["id-5"]],
				[t: "p", id: "id-7", lat: 7D, lng: 0D, color: "rgb(233, 87, 15)"],
				[t: "p", id: "id-1", lat: 11D, lng: 0D, color: "rgb(233, 87, 15)"],
				[t: "d", list: ["id-6", "id-3"]],
				[t: "d", list: ["id-7", "id-1"]],
			]
		]

		expect:
		new ModuleTestHelper.Builder(module, inputValues, outputValues)
			.uiChannelMessages(channelMessages)
			.ticks(ticks)
			.extraIterationsAfterInput(3)
			.test()
	}
}
