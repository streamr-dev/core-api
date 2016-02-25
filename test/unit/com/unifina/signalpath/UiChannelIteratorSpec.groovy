package com.unifina.signalpath

import com.unifina.domain.signalpath.Module
import grails.test.mixin.Mock
import spock.lang.Specification

@Mock([Module])
class UiChannelIteratorSpec extends Specification {

	Module switcher = new Module()
	Module table = new Module()

	def setup() {
		switcher.save(validate: false)
		table.save(validate: false)
	}

	def "parses 0 elements given empty map"() {
		when:
		List<UiChannelIterator.Element> elements = UiChannelIterator.over([:]).collect()

		then:
		elements.empty
	}



	def "parses 1 elements if only root and no modules"() {
		def signalPathMap = [
			uiChannel: [
			    id: "666",
				name: "Notifications"
			]
		]

		when:
		List<UiChannelIterator.Element> elements = UiChannelIterator.over(signalPathMap).collect()

		then:
		elements.size() == 1
		elements[0].id == "666"
		elements[0].name == "Notifications"
		elements[0].rootElement
		elements[0].module == null
		elements[0].hash == null
		elements[0].uiChannelData == signalPathMap.uiChannel
	}

	def "parses 1 elements if not root and a moudle with uiChannel"() {
		def signalPathMap = [
			modules: [
				[
					hash: 2320,
					id: switcher.id,
					uiChannel: [
						id: "777",
						name: "Switcher"
					]
				]
			]
		]

		when:
		List<UiChannelIterator.Element> elements = UiChannelIterator.over(signalPathMap).collect()

		then:
		elements.size() == 1
		elements*.id == ["777"]
	}

	def "parses root and modules with uiChannel"() {
		def signalPathMap = [
			uiChannel: [
				id: "666",
				name: "Notifications"
			],
			modules: [
				[
					hash: 1539,
					id: 64
				],
				[
				    hash: 2320,
					id: switcher.id,
					uiChannel: [
					    id: "777",
						name: "Switcher"
					]
				],
				[
				    hash: 4514,
					id: table.id,
					uiChannel: [
					    id: "888",
						name: "Table"
					]
				],
				[
				    hash: 9781,
					id: 60,
				]
			]
		]

		when:
		List<UiChannelIterator.Element> elements = UiChannelIterator.over(signalPathMap).collect()

		then:
		elements*.id == ["666", "777", "888"]
		elements*.name == ["Notifications", "Switcher", "Table"]
		elements*.hash == [null, "2320", "4514"]
		elements*.module == [null, switcher, table]
		elements*.uiChannelData == [
			signalPathMap.uiChannel,
			signalPathMap.modules[1].uiChannel,
			signalPathMap.modules[2].uiChannel,
		]
	}
}
