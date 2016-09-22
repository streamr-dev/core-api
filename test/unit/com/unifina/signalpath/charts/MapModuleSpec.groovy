package com.unifina.signalpath.charts

import com.unifina.utils.StreamrColor
import com.unifina.utils.testutils.ModuleTestHelper;
import spock.lang.Specification;

public class MapModuleSpec extends Specification {
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
}
