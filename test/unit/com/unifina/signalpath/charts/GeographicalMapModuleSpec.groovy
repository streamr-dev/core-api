package com.unifina.signalpath.charts

import com.unifina.UiChannelMockingSpecification
import com.unifina.domain.security.SecUser
import com.unifina.utils.Globals
import com.unifina.utils.StreamrColor
import com.unifina.utils.testutils.FakeIdGenerator
import com.unifina.utils.testutils.ModuleTestHelper
import grails.test.mixin.Mock
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin

@TestMixin(GrailsUnitTestMixin)
@Mock([SecUser])
class GeographicalMapModuleSpec extends UiChannelMockingSpecification {

	def setup() {
		mockServicesForUiChannels()
	}

	Map inputValues = [
		traceColor: [new StreamrColor(233, 87, 15), null, null, new StreamrColor(6, 6, 6)],
		id: ["id-1", "id-2", "id-1", "id-3"],
		latitude: [60.412, 59.666, 30.000D, 0D]*.doubleValue(),
		longitude: [24.079, 66.999, -21.758D, 1D]*.doubleValue(),
	]

	void "MapModule pushes correct data to uiChannel (with drawTrace = true)"() {
		def module = setupModule(new GeographicalMapModule(), [
			uiChannel: [id: "mapPointData"],
			options: [
				drawTrace: [value: true],
			]
		])

		Map outputValues = [:]
		Map channelMessages = [
			mapPointData: [
				[t: "p", id: "id-1", lat: 60.412D, lng: 24.079D, color: "rgba(233, 87, 15, 1.0)", tracePointId: "id-0"],
				[t: "p", id: "id-2", lat: 59.666D, lng: 66.999D, color: "rgba(233, 87, 15, 1.0)", tracePointId: "id-1"],
				[t: "p", id: "id-1", lat: 30.000D, lng: -21.758D, color: "rgba(233, 87, 15, 1.0)", tracePointId: "id-2"],
				[t: "p", id: "id-3", lat: 0D, lng: 1D, color: "rgba(6, 6, 6, 1.0)", tracePointId: "id-3"],
			]
		]

		expect:
		new ModuleTestHelper.Builder(module, inputValues, outputValues)
			.uiChannelMessages(channelMessages, getSentMessagesByStreamId())
			.overrideGlobals { Globals g ->
				g.setIdGenerator(new FakeIdGenerator())
				return g
			}
			.test()
	}

	void "MapModule pushes correct data to uiChannel (with customMarkerLabel=true)"() {
		def module = setupModule(new GeographicalMapModule(), [
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
				[t: "p", id: "id-1", label: "label", lat: 60.412D, lng: 24.079D, color: "rgba(233, 87, 15, 1.0)", tracePointId: "id-0"],
				[t: "p", id: "id-2", label: "label", lat: 59.666D, lng: 66.999D, color: "rgba(233, 87, 15, 1.0)", tracePointId: "id-1"],
				[t: "p", id: "id-1", label: "label2", lat: 30.000D, lng: -21.758D, color: "rgba(233, 87, 15, 1.0)", tracePointId: "id-2"],
				[t: "p", id: "id-3", label: "label3", lat: 0D, lng: 1D, color: "rgba(6, 6, 6, 1.0)", tracePointId: "id-3"],
			]
		]

		expect:
		new ModuleTestHelper.Builder(module, inputValues, outputValues)
			.uiChannelMessages(channelMessages, getSentMessagesByStreamId())
			.overrideGlobals { Globals g ->
				g.setIdGenerator(new FakeIdGenerator())
				return g
			}
			.test()
	}

	void "MapModule works correctly with expiring markers"() {
		def module = setupModule(new GeographicalMapModule(), [
			uiChannel: [id: "mapPointData"],
			options: [
				drawTrace: [value: true],
				expiringTimeOfMarkerInSecs: [value: 2]
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
				[t: "p", id: "id-1", lat: 1D, lng: 0D, color: "rgba(233, 87, 15, 1.0)", tracePointId: "id-0"],
				[t: "p", id: "id-2", lat: 2D, lng: 0D, color: "rgba(233, 87, 15, 1.0)", tracePointId: "id-1"],
				[t: "p", id: "id-3", lat: 3D, lng: 0D, color: "rgba(233, 87, 15, 1.0)", tracePointId: "id-2"],
				[t: "p", id: "id-4", lat: 4D, lng: 0D, color: "rgba(233, 87, 15, 1.0)", tracePointId: "id-3"],
				[t: "d", markerList: ["id-1"], pointList: []],
				[t: "p", id: "id-3", lat: 33D, lng: 0D, color: "rgba(233, 87, 15, 1.0)", tracePointId: "id-4"],
				[t: "p", id: "id-5", lat: 5D, lng: 0D, color: "rgba(233, 87, 15, 1.0)", tracePointId: "id-5"],
				[t: "p", id: "id-6", lat: 6D, lng: 0D, color: "rgba(233, 87, 15, 1.0)", tracePointId: "id-6"],
				[t: "d", markerList: ["id-2", "id-4"], pointList: []],
				[t: "p", id: "id-6", lat: 66D, lng: 0D, color: "rgba(233, 87, 15, 1.0)", tracePointId: "id-7"],
				[t: "p", id: "id-3", lat: 333D, lng: 0D, color: "rgba(233, 87, 15, 1.0)", tracePointId: "id-8"],
				[t: "d", markerList: ["id-5"], pointList: []],
				[t: "p", id: "id-7", lat: 7D, lng: 0D, color: "rgba(233, 87, 15, 1.0)", tracePointId: "id-9"],
				[t: "p", id: "id-1", lat: 11D, lng: 0D, color: "rgba(233, 87, 15, 1.0)", tracePointId: "id-10"],
				[t: "d", markerList: ["id-6", "id-3"], pointList: []],
				[t: "d", markerList: ["id-7", "id-1"], pointList: []],
			]
		]

		expect:
		new ModuleTestHelper.Builder(module, inputValues, outputValues)
			.uiChannelMessages(channelMessages, getSentMessagesByStreamId())
			.ticks(ticks)
			.extraIterationsAfterInput(3)
			.overrideGlobals { Globals g ->
				g.setIdGenerator(new FakeIdGenerator())
				return g
			}
			.test()
	}

	void "MapModule works correctly with expiring (trace)points"() {
		def module = setupModule(new GeographicalMapModule(), [
				uiChannel: [id: "mapPointData"],
				options: [
						drawTrace: [value: true],
						expiringTimeOfTraceInSecs: [value: 2]
				]
		])
		Map inputValues = [
				//			  0	   1    2    3    4    5    6    7    8    9    10
				id: 		["1", "2", "3", "1", "2", "3", "2", "3", "1", "2", "3"],
				latitude: 	[ 1,   2, 	3, 	 10,  20,  30, 	21,	 31,  11,  22, 	32]*.doubleValue(),
				longitude: 	[ 0,   0,   0, 	 0,	  0,   0,   0,   0,   0,   0,   0]*.doubleValue(),
		]
		Map<Integer, Date> ticks = [
				0: new Date(0),
				1: new Date(1000),
				4: new Date(2000),
				7: new Date(3000),
				9: new Date(4000),
				10: new Date(5000)
		]
		Map outputValues = [:]
		Map channelMessages = [
				mapPointData: [
						[t: "p", id: "1", lat: 1D,  lng: 0D, color: "rgba(233, 87, 15, 1.0)", tracePointId: "id-0"], // 0 /0s
						[t: "p", id: "2", lat: 2D,  lng: 0D, color: "rgba(233, 87, 15, 1.0)", tracePointId: "id-1"], // 1 /1s
						[t: "p", id: "3", lat: 3D,  lng: 0D, color: "rgba(233, 87, 15, 1.0)", tracePointId: "id-2"], // 2
						[t: "p", id: "1", lat: 10D, lng: 0D, color: "rgba(233, 87, 15, 1.0)", tracePointId: "id-3"], // 3
						[t: "d", pointList: ['id-0'], markerList: []],                                         // 4 /2s
						[t: "p", id: "2", lat: 20D, lng: 0D, color: "rgba(233, 87, 15, 1.0)", tracePointId: "id-4"], // 4
						[t: "p", id: "3", lat: 30D, lng: 0D, color: "rgba(233, 87, 15, 1.0)", tracePointId: "id-5"], // 5
						[t: "p", id: "2", lat: 21D, lng: 0D, color: "rgba(233, 87, 15, 1.0)", tracePointId: "id-6"], // 6
						[t: "d", pointList: ['id-1', 'id-2', 'id-3'], markerList: []],	                       // 7 /3s
						[t: "p", id: "3", lat: 31D, lng: 0D, color: "rgba(233, 87, 15, 1.0)", tracePointId: "id-7"], // 7
						[t: "p", id: "1", lat: 11D, lng: 0D, color: "rgba(233, 87, 15, 1.0)", tracePointId: "id-8"], // 8
						[t: "d", pointList: ["id-4", "id-5", "id-6"], markerList: []],                         // 9 /4s
						[t: "p", id: "2", lat: 22D, lng: 0D, color: "rgba(233, 87, 15, 1.0)", tracePointId: "id-9"], // 9
						[t: "d", pointList: ["id-7", "id-8"], markerList: []],      				           // 10 /5s
						[t: "p", id: "3", lat: 32D, lng: 0D, color: "rgba(233, 87, 15, 1.0)", tracePointId: "id-10"] // 10 /
				]
		]
		expect:
		new ModuleTestHelper.Builder(module, inputValues, outputValues)
				.uiChannelMessages(channelMessages, getSentMessagesByStreamId())
				.ticks(ticks)
				.overrideGlobals { Globals g ->
					g.setIdGenerator(new FakeIdGenerator())
					return g
				}
				.test()
	}
}
